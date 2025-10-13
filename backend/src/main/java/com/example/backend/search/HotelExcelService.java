package com.example.backend.search;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Excel → Hotel/Room/Service/Images Importer
 * - 객실명이 제각각이어도 가격이 맞도록 캐논 매핑 + 퍼지 매칭 + 다양한 포맷 지원
 */
@Service
public class HotelExcelService {

    private static final Logger log = LoggerFactory.getLogger(HotelExcelService.class);

    private final HotelRepository hotelRepository;
    private final ServiceRepository serviceRepository;
    private final RoomRepository roomRepository;
    private final HotelImageRepository hotelImageRepository;

    @Value("${file.upload-dir}")
    private String imageBaseDirectoryPath; // 예: D:/hotel_image/

    public HotelExcelService(HotelRepository hotelRepository,
                             ServiceRepository serviceRepository,
                             RoomRepository roomRepository,
                             HotelImageRepository hotelImageRepository) {
        this.hotelRepository = hotelRepository;
        this.serviceRepository = serviceRepository;
        this.roomRepository = roomRepository;
        this.hotelImageRepository = hotelImageRepository;
    }

    // ───────────────────── 공통 유틸 ─────────────────────

    /** 헤더/키 비교용: 유니코드 정규화 + 보이지 않는 문자 제거 + 공백제거 + 소문자 */
    private String norm(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.replaceAll("[\\u200B-\\u200D\\uFEFF\\u00A0]", ""); // 제로폭/NBSP
        t = t.replaceAll("\\s+", "").toLowerCase();
        return t;
    }

    /** 객실 타입 문자열 정규화: 괄호/인원/꼬리표 제거 + 평형 통일 + 동의어 치환(기초) */
    private String normType(String s){
        if (s == null) return "";
        String t = s;

        // 괄호 내용/인원 꼬리표 제거
        t = t.replaceAll("\\(.*?\\)", "");
        t = t.replaceAll("최대\\s*\\d+\\s*명", "");
        t = t.replaceAll("\\d+\\s*명", "");
        t = t.replaceAll("\\s+","").toLowerCase();

        // 평형 표기 통일
        t = t.replaceAll("(\\d+)\\s*평형", "$1평");
        t = t.replace("평형", "평");

        // 불필요 꼬리표 제거
        t = t.replace("객실","").replace("룸","").replace("타입","");
        t = t.replaceAll("실$",""); // 일반실→일반, 특실→특, 한실→한

        // 동의어/영문
        t = t.replace("온돌","한");
        t = t.replace("양실","양");
        t = t.replace("한실","한");
        t = t.replace("스탠다드","일반").replace("standard","일반");
        t = t.replace("디럭스","특").replace("deluxe","특");
        t = t.replace("western","양");
        t = t.replace("suite","스위트");

        return t;
    }

    /** 객실 타입을 공통 코드로 매핑: std/ dlx/ kor/ suite/ (또는 10평 등 숫자평) */
    private String canonicalType(String s){
        String t = normType(s);

        // 숫자 평형 우선
        Matcher m = Pattern.compile("(\\d+)평").matcher(t);
        if (m.find()) return m.group(1) + "평";

        if (t.contains("스위트")) return "suite"; // 스위트 계열

        // 한실/온돌 계열
        if (t.contains("한") || t.contains("온돌")) return "kor";

        // 특/디럭스 계열
        if (t.contains("특")) return "dlx";

        // 일반/스탠다드/양실(=서양식) → std
        if (t.contains("일반") || t.contains("양")) return "std";

        // 못맞춘 경우 원본 유지(추가 룰 확장 여지)
        return t;
    }

    /** 헤더 맵 구성 */
    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> m = new HashMap<>();
        if (headerRow == null) return m;
        for (Cell c : headerRow) {
            if (c != null) {
                String key = c.toString();
                if (key != null && !key.trim().isEmpty()) {
                    m.put(norm(key), c.getColumnIndex());
                }
            }
        }
        return m;
    }

    /** 여러 후보 키 중 먼저 매칭되는 컬럼 인덱스 반환(없으면 -1) */
    private int col(Map<String, Integer> headerMap, String... keys) {
        for (String k : keys) {
            Integer idx = headerMap.get(norm(k));
            if (idx != null) return idx;
        }
        return -1;
    }

    // ───────────────────── 가격 파싱/매칭 ─────────────────────

    /** 상세 텍스트에서 가격 파싱 (쉼표/원/KRW 허용) */
    private int parsePriceFromDetail(String detail) {
        if (detail == null || detail.isEmpty()) return 0;
        Matcher m = Pattern.compile("(\\d{1,3}(?:,\\d{3})+|\\d+)\\s*(?:원|krw)?",
                Pattern.CASE_INSENSITIVE).matcher(detail);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1).replace(",", "")); }
            catch (NumberFormatException ignore) {}
        }
        return 0;
    }

    /**
     * "타입:가격" / "타입=가격" / "타입(가격)" / "타입 가격" / "타입-가격"
     * 구분자: , / | ; 줄바꿈
     */
    private Map<String,Integer> parseTypeIntMap(String text){
        Map<String,Integer> map = new HashMap<>();
        if (text == null || text.trim().isEmpty()) return map;

        String[] parts = text.split("[,;/|\\n\\r]+");

        Pattern kv1 = Pattern.compile("^(.+?)[=:]\\s*(\\d{1,3}(?:,\\d{3})+|\\d+)\\s*(?:원|krw)?", Pattern.CASE_INSENSITIVE);
        Pattern kv2 = Pattern.compile("^(.+?)\\s*\\((\\d{1,3}(?:,\\d{3})+|\\d+)\\s*(?:원|krw)?\\)", Pattern.CASE_INSENSITIVE);
        Pattern kv3 = Pattern.compile("^(.+?)\\s*[-~]?\\s*(\\d{1,3}(?:,\\d{3})+|\\d+)\\s*(?:원|krw)?$", Pattern.CASE_INSENSITIVE);

        for (String raw : parts){
            String t = raw.trim();
            if (t.isEmpty()) continue;

            Matcher m = kv1.matcher(t);
            if (!m.find()) m = kv2.matcher(t);
            if (!m.find()) m = kv3.matcher(t);
            if (m.find()){
                String type = m.group(1).trim();
                String num  = m.group(2).replace(",", "");
                try { map.put(canonicalType(type), Integer.parseInt(num)); }
                catch (NumberFormatException ignore) {}
            }
        }
        return map;
    }

    /** 텍스트 안의 숫자(가격)만 순서대로 뽑기 */
    private List<Integer> extractNumbers(String text){
        List<Integer> out = new ArrayList<>();
        if (text == null) return out;
        Matcher m = Pattern.compile("(\\d{1,3}(?:,\\d{3})+|\\d+)").matcher(text);
        while (m.find()) {
            try { out.add(Integer.parseInt(m.group(1).replace(",", ""))); }
            catch (NumberFormatException ignore) {}
        }
        return out;
    }

    /** 토큰셋(한글/영문/숫자평) */
    private Set<String> tokens(String s){
        String t = canonicalType(s);
        Set<String> out = new HashSet<>();
        Matcher m = Pattern.compile("([가-힣]+|[a-z]+|\\d+평|\\d+)").matcher(t);
        while (m.find()) out.add(m.group(1));
        return out;
    }

    /** 간단 토큰셋 자카드 유사도 */
    private double jaccard(Set<String> a, Set<String> b){
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(a); inter.retainAll(b);
        Set<String> union = new HashSet<>(a); union.addAll(b);
        return (double) inter.size() / (double) union.size();
    }

    /** 타입별 가격 퍼지 조회 */
    private Integer lookupTypePrice(Map<String,Integer> map, String roomType) {
        if (map == null || map.isEmpty()) return null;

        String key = canonicalType(roomType);

        // 1) 정확
        Integer p = map.get(key);
        if (p != null) return p;

        // 2) 포함/역포함 (가장 긴 키 우선)
        String bestKey = null;
        for (String k : map.keySet()) {
            if (key.contains(k) || k.contains(key)) {
                if (bestKey == null || k.length() > bestKey.length()) bestKey = k;
            }
        }
        if (bestKey != null) return map.get(bestKey);

        // 3) 토큰셋 유사도
        double bestScore = 0.0;
        String bestTK = null;
        Set<String> tk = tokens(key);
        for (String k : map.keySet()) {
            double sc = jaccard(tk, tokens(k));
            if (sc > bestScore) { bestScore = sc; bestTK = k; }
        }
        return (bestScore >= 0.5 && bestTK != null) ? map.get(bestTK) : null;
    }

    // ───────────────────── 타입별 가격 맵 수집기 ─────────────────────

    /** A) '객실별 가격' 한 칸에 몰아쓴 경우 */
    private Map<String,Integer> collectFromSingleTextColumn(Row row, Map<String,Integer> headerMap){
        Map<String,Integer> out = new HashMap<>();
        int idxTypePrices = col(headerMap, "객실별 가격","객실가격(타입별)","room prices","roomprices");
        if (idxTypePrices < 0) {
            for (Map.Entry<String,Integer> e : headerMap.entrySet()) {
                String k = e.getKey();
                if (k.contains("객실") && k.contains("가격") && !k.equals("가격")) {
                    idxTypePrices = e.getValue(); break;
                }
            }
        }
        if (idxTypePrices >= 0) {
            out.putAll(parseTypeIntMap(getCellString(row, idxTypePrices)));
        }
        return out;
    }

    /** B) '○○가격' 개별 컬럼 또는 헤더가 타입명이고 값이 숫자인 경우 */
    private Map<String,Integer> collectFromHeaderPerType(Row row, Map<String,Integer> headerMap){
        Map<String,Integer> out = new HashMap<>();
        for (Map.Entry<String,Integer> e : headerMap.entrySet()) {
            String k = e.getKey();
            int idx = e.getValue();

            // 명백히 배제
            if (k.contains("주소") || k.contains("위도") || k.contains("경도") || k.contains("전화")
                || k.contains("소개") || k.contains("개요") || k.equals("가격") || k.contains("최저")
                || k.contains("region") || k.contains("address")) continue;

            // 1) 라벨+가격
            if (k.contains("가격") || k.contains("요금") || k.contains("price")) {
                String label = k.replace("가격","").replace("요금","").replace("price","");
                label = label.replaceAll("[_:\\-]+", "");
                if (!label.isEmpty()) {
                    int v = getCellInt(row, idx);
                    if (v > 0) out.put(canonicalType(label), v);
                }
                continue;
            }

            // 2) 헤더 자체가 타입명 & 값이 숫자인 경우
            int v = getCellInt(row, idx);
            if (v > 0) out.put(canonicalType(k), v);
        }
        return out;
    }

    /** C) '객실명1/객실가격1' 같은 짝수 컬럼 */
    private Map<String,Integer> collectFromPairedColumns(Row row, Map<String,Integer> headerMap){
        Map<String,Integer> out = new HashMap<>();

        List<Map.Entry<String,Integer>> nameCols = headerMap.entrySet().stream()
                .filter(e -> e.getKey().matches(".*(객실명|객실유형|객실타입|룸명|roomname|roomtype).*\\d*"))
                .toList();

        if (nameCols.isEmpty()) return out;

        for (Map.Entry<String,Integer> nameEntry : nameCols) {
            String nk = nameEntry.getKey();
            Integer ni = nameEntry.getValue();
            String suffix = nk.replaceAll(".*?(\\d+)$", "$1");

            Integer pi = null;
            for (Map.Entry<String,Integer> e : headerMap.entrySet()) {
                String hk = e.getKey();
                if (!hk.contains("가격") && !hk.contains("요금") && !hk.contains("price")) continue;

                if (hk.matches(".*\\d+$")) {
                    String sx = hk.replaceAll(".*?(\\d+)$", "$1");
                    if (sx.equals(suffix)) { pi = e.getValue(); break; }
                } else {
                    if (pi == null) pi = e.getValue();
                }
            }
            if (pi == null) continue;

            String typeName = getCellString(row, ni);
            int price = getCellInt(row, pi);
            if (!typeName.isEmpty() && price > 0) {
                out.put(canonicalType(typeName), price);
            }
        }
        return out;
    }

    /** D) '객실 유형'과 '객실별 가격'이 라벨 없이 숫자만 있을 때 순서 매핑 */
    private Map<String,Integer> collectFromOrderedList(Row row, Map<String,Integer> headerMap){
        Map<String,Integer> out = new HashMap<>();

        int idxTypes  = col(headerMap, "객실 유형", "객실타입", "room type", "room types", "객실명","룸명");
        int idxPrices = col(headerMap, "객실별 가격","객실가격(타입별)","room prices","roomprices");
        if (idxTypes < 0 || idxPrices < 0) return out;

        String typesStr = getCellString(row, idxTypes);
        if (typesStr == null || typesStr.trim().isEmpty()) return out;

        String[] types = typesStr.split("[,;/|·ㆍ・、\\n\\r()\\[\\]]+");
        List<Integer> prices = extractNumbers(getCellString(row, idxPrices));
        if (prices.size() != types.length) return out; // 개수가 맞을 때만

        for (int i = 0; i < types.length; i++){
            String type = types[i].trim();
            if (type.isEmpty()) continue;
            out.put(canonicalType(type), prices.get(i));
        }
        if (!out.isEmpty() && log.isDebugEnabled()){
            log.debug("순서매핑 적용: {}", out);
        }
        return out;
    }

    /** 위 수집기들을 합쳐 최종 타입별 가격 맵 생성 */
    private Map<String,Integer> buildTypePriceMap(Row row, Map<String,Integer> headerMap){
        Map<String,Integer> A = collectFromSingleTextColumn(row, headerMap);
        Map<String,Integer> B = collectFromHeaderPerType(row, headerMap);
        Map<String,Integer> C = collectFromPairedColumns(row, headerMap);
        Map<String,Integer> D = collectFromOrderedList(row, headerMap);

        Map<String,Integer> result = new HashMap<>();
        result.putAll(A);
        result.putAll(B);
        result.putAll(C);
        // 라벨 없는 순서맵은 보충용
        for (Map.Entry<String,Integer> e : D.entrySet()){
            result.putIfAbsent(e.getKey(), e.getValue());
        }
        if (log.isDebugEnabled()) log.debug("typePriceMap={}", result);
        return result;
    }

    // ───────────────────── 메인 임포트 ─────────────────────

    @Transactional
    public void importHotelsFromExcel(String filePath, String hotelType) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                if (sheet == null) continue;

                Row headerRow = sheet.getRow(0);
                Map<String, Integer> headerMap = buildHeaderMap(headerRow);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    // ───────── 기본 정보 ─────────
                    String hotelName = getCellString(row, col(headerMap, "명칭", "이름", "name"));
                    String hotelAddress = getCellString(row, col(headerMap, "주소", "address"));
                    if (hotelName.isEmpty()) continue;

                    Hotel hotel = hotelRepository.findByhNameAndAddress(hotelName, hotelAddress)
                            .orElseGet(() -> {
                                Hotel h = new Hotel();
                                h.setHName(hotelName);
                                if (hotelAddress != null && !hotelAddress.isEmpty()) {
                                    String[] parts = hotelAddress.split(" ");
                                    h.setRegion(parts.length >= 2 ? parts[0] + " " + parts[1] : parts[0]);
                                }
                                h.setAddress(hotelAddress);
                                h.setLatitude(getCellDouble(row, col(headerMap, "위도", "lat", "latitude")));
                                h.setLongitude(getCellDouble(row, col(headerMap, "경도", "lng", "longitude")));
                                h.setInfo(getCellString(row, col(headerMap, "개요", "소개", "overview", "description")));
                                h.setType(hotelType);
                                if (h.getRooms() == null) h.setRooms(new ArrayList<>());
                                if (h.getImages() == null) h.setImages(new ArrayList<>());
                                return hotelRepository.save(h);
                            });

                    // ───────── 편의시설 ─────────
                    Set<ServiceEntity> serviceEntities = new HashSet<>();
                    String detailInfo = getCellString(row, col(headerMap, "상세정보", "상세", "detail", "description"));

                    String facilitiesText = getCellString(row, col(headerMap, "부대 시설", "부대시설"));
                    if (!facilitiesText.isEmpty()) {
                        String[] facilities = facilitiesText.split("[,;/|]+");
                        for (String f : facilities) {
                            String v = f.trim();
                            if (!v.isEmpty()) serviceEntities.add(findOrCreateService(v));
                        }
                    }

                    String[] amenityHeaders = {
                            "주차 가능","조리 가능","픽업서비스","식음료장","세미나","스포츠시설","사우나실","뷰티 시설",
                            "노래방","바베큐장","캠프화이어","자전거대여","휘트니스센터","공용 PC실","공용 샤워실"
                    };
                    for (String hname : amenityHeaders) {
                        int idx = col(headerMap, hname);
                        if (idx >= 0) {
                            String v = getCellString(row, idx);
                            if ("있음".equals(v) || "가능".equals(v) || "Y".equalsIgnoreCase(v)) {
                                serviceEntities.add(findOrCreateService(hname));
                            }
                        }
                    }
                    if (!detailInfo.isEmpty()) {
                        Pattern amenityPattern = Pattern.compile("([^\\n\\r:]+?)\\s*:\\s*Y");
                        Matcher matcher = amenityPattern.matcher(detailInfo);
                        while (matcher.find()) {
                            String amenityName = matcher.group(1).trim();
                            if (!amenityName.isEmpty()) serviceEntities.add(findOrCreateService(amenityName));
                        }
                    }
                    if (!serviceEntities.isEmpty()) hotel.setServices(new ArrayList<>(serviceEntities));

                    // ───────── 공통 룸 속성 ─────────
                    String checkinTime  = parseFirstTime(getCellString(row, col(headerMap, "체크인", "checkin", "입실시간")), "18:00");
                    String checkoutTime = parseFirstTime(getCellString(row, col(headerMap, "체크아웃", "checkout", "퇴실시간")), "13:00");

                    // 공통가
                    int idxPrice = col(headerMap, "가격","객실가격","요금","요금(원)","price","minprice","최저가");
                    int commonPrice = getCellInt(row, idxPrice);
                    if (commonPrice == 0) commonPrice = parsePriceFromDetail(detailInfo);

                    // 타입별 가격 맵
                    Map<String,Integer> typePriceMap = buildTypePriceMap(row, headerMap);

                    // 공통 인원/객실수
                    int commonPeople = getCellInt(row, col(headerMap, "수용 가능 인원", "정원", "인원", "people"));
                    if (commonPeople < 1) commonPeople = 2;
                    int commonCount = getCellInt(row, col(headerMap, "객실 수", "rooms", "객실수"));
                    if (commonCount < 1) commonCount = 1;

                    // 객실 타입 목록
                    String roomTypesString = getCellString(row, col(headerMap, "객실 유형", "객실타입", "room type", "room types", "객실명","룸명"));
                    if (roomTypesString.isEmpty()) {
                        // 헤더에서 타입을 유추(가격 맵의 키들을 타입으로 사용)
                        if (!typePriceMap.isEmpty()) {
                            roomTypesString = String.join(",", typePriceMap.keySet());
                        } else {
                            roomTypesString = "기본 객실";
                        }
                    }
                    String[] roomTypeArray = roomTypesString.split("[,;/|·ㆍ・、\\n\\r()\\[\\]]+");

                    // 기존 룸 맵
                    List<Room> rooms = hotel.getRooms();
                    if (rooms == null) { rooms = new ArrayList<>(); hotel.setRooms(rooms); }
                    Map<String, Room> existingRoomsMap = rooms.stream()
                            .collect(Collectors.toMap(Room::getType, Function.identity(), (a, b) -> a));

                    for (String roomType : roomTypeArray) {
                        String finalRoomType = roomType.trim();
                        if (finalRoomType.isEmpty()) continue;

                        Room room = existingRoomsMap.get(finalRoomType);
                        if (room == null) {
                            room = new Room();
                            room.setHotel(hotel);
                            room.setType(finalRoomType);
                            rooms.add(room);
                        }

                        // 타입별 가격 결정
                        Integer typePrice = lookupTypePrice(typePriceMap, finalRoomType);
                        int priceForThis = (typePrice != null && typePrice > 0) ? typePrice : commonPrice;
                        if (priceForThis > 0) room.setPrice(priceForThis);

                        if (log.isDebugEnabled()) {
                            log.debug("[{}] roomType='{}' → price={} (typePrice={}, common={})",
                                    hotelName, finalRoomType, room.getPrice(), typePrice, commonPrice);
                        }

                        // 공통 속성
                        room.setPeople(commonPeople);
                        room.setCount(commonCount);
                        room.setCheckinTime(checkinTime);
                        room.setCheckoutTime(checkoutTime);
                        roomRepository.save(room);

                        // 객실 이미지 매핑
                        String dynamicImageDirectoryPath = imageBaseDirectoryPath + hotel.getType() + "/";
                        String roomImageDirectoryPath = dynamicImageDirectoryPath + "rooms/";
                        File roomImageDir = new File(roomImageDirectoryPath);
                        Integer roomId = room.getRId();

                        if (roomId != null && roomImageDir.exists() && roomImageDir.isDirectory()) {
                            String roomIdPrefix = roomId.toString();
                            File[] matchingRoomFiles = roomImageDir.listFiles((dir, name) ->
                                    name.startsWith(roomIdPrefix + ".") || name.startsWith(roomIdPrefix + "_")
                            );

                            if (matchingRoomFiles != null) {
                                for (File roomImageFile : matchingRoomFiles) {
                                    String filename = roomImageFile.getName();
                                    boolean imageExists = (room.getImages() != null) && room.getImages().stream()
                                            .anyMatch(img -> img.getFilename() != null && img.getFilename().equals(filename));

                                    if (!imageExists) {
                                        HotelImage image = new HotelImage();
                                        image.setFilename(filename);
                                        image.setImageUrl("/images/" + hotel.getType() + "/rooms/" + filename);
                                        image.setImageType("room");
                                        image.setRoom(room);
                                        image.setHotel(hotel);
                                        if (room.getImages() == null) room.setImages(new ArrayList<>());
                                        room.getImages().add(image);
                                        hotelImageRepository.save(image);
                                    }
                                }
                            }
                        }
                    }

                    // 호텔 메인/서브 이미지 매핑
                    Long hotelId = hotel.getHId();
                    String dynamicImageDirectoryPath = imageBaseDirectoryPath + hotel.getType() + "/";
                    File imageDir = new File(dynamicImageDirectoryPath);
                    if (hotel.getImages() == null) hotel.setImages(new ArrayList<>());

                    if (hotelId != null && imageDir.exists() && imageDir.isDirectory()) {
                        String idPrefix = hotelId.toString();
                        File[] matchingFiles = imageDir.listFiles((dir, name) ->
                                name.startsWith(idPrefix + ".") || name.startsWith(idPrefix + "_")
                        );

                        if (matchingFiles != null) {
                            for (File imageFile : matchingFiles) {
                                String filename = imageFile.getName();
                                boolean imageExists = hotel.getImages().stream()
                                        .anyMatch(img -> img.getFilename() != null && img.getFilename().equals(filename));

                                if (!imageExists) {
                                    HotelImage image = new HotelImage();
                                    image.setFilename(filename);
                                    image.setImageUrl("/images/" + hotel.getType() + "/" + filename);
                                    image.setImageType(filename.contains("_") ? "sub" : "main");
                                    image.setHotel(hotel);
                                    hotel.getImages().add(image);
                                    hotelImageRepository.save(image);
                                }
                            }
                        }
                    }

                    // (선택) 호텔 최저가 갱신
                    try {
                        int min = hotel.getRooms().stream()
                                .filter(r -> r.getPrice() != null && r.getPrice() > 0)
                                .mapToInt(Room::getPrice).min().orElse(0);
                        // hotel.setMinPrice(min);
                    } catch (Exception ignore) {}

                    hotelRepository.save(hotel);
                }
            }
        }
    }

    // ───────────────────── 기타 유틸 ─────────────────────

    private String parseFirstTime(String text, String defaultValue) {
        if (text == null || text.isEmpty()) return defaultValue;
        Matcher matcher = Pattern.compile("\\d{2}:\\d{2}").matcher(text);
        return matcher.find() ? matcher.group() : defaultValue;
    }

    private ServiceEntity findOrCreateService(String serviceName) {
        return serviceRepository.findByServiceName(serviceName)
                .orElseGet(() -> {
                    ServiceEntity n = new ServiceEntity();
                    n.setServiceName(serviceName);
                    return serviceRepository.save(n);
                });
    }

    private String getCellString(Row row, int index) {
        if (row == null || index < 0) return "";
        Cell cell = row.getCell(index);
        return (cell == null) ? "" : cell.toString().trim();
    }

    private Integer getCellInt(Row row, int index) {
        if (row == null || index < 0) return 0;
        Cell cell = row.getCell(index);
        if (cell == null || cell.toString().trim().isEmpty()) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        try {
            String numStr = cell.toString().replaceAll("[^0-9.]", "");
            if (numStr.isEmpty()) return 0;
            if (numStr.contains(".")) numStr = numStr.substring(0, numStr.indexOf('.'));
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double getCellDouble(Row row, int index) {
        if (row == null || index < 0) return null;
        Cell cell = row.getCell(index);
        if (cell == null || cell.toString().trim().isEmpty()) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        try { return Double.parseDouble(cell.toString().trim()); }
        catch (Exception e) { return null; }
    }
}