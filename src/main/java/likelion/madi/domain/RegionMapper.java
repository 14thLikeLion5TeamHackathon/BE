package likelion.madi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

public class RegionMapper {

    private static final Map<String, Coordinate> LOCATION_MAP = new HashMap<>();

    static {
        // ==========================================
        // 1. 서울특별시 (25개 자치구)
        // ==========================================
        LOCATION_MAP.put("서울특별시_강남구", new Coordinate(37.4979, 127.0276));
        LOCATION_MAP.put("서울특별시_강동구", new Coordinate(37.5301, 127.1238));
        LOCATION_MAP.put("서울특별시_강북구", new Coordinate(37.6396, 127.0257));
        LOCATION_MAP.put("서울특별시_강서구", new Coordinate(37.5509, 126.8495));
        LOCATION_MAP.put("서울특별시_관악구", new Coordinate(37.4784, 126.9516));
        LOCATION_MAP.put("서울특별시_광진구", new Coordinate(37.5385, 127.0823));
        LOCATION_MAP.put("서울특별시_구로구", new Coordinate(37.4954, 126.8874));
        LOCATION_MAP.put("서울특별시_금천구", new Coordinate(37.4568, 126.8955));
        LOCATION_MAP.put("서울특별시_노원구", new Coordinate(37.6542, 127.0568));
        LOCATION_MAP.put("서울특별시_도봉구", new Coordinate(37.6688, 127.0471));
        LOCATION_MAP.put("서울특별시_동대문구", new Coordinate(37.5744, 127.0400));
        LOCATION_MAP.put("서울특별시_동작구", new Coordinate(37.5124, 126.9393));
        LOCATION_MAP.put("서울특별시_마포구", new Coordinate(37.5663, 126.9016));
        LOCATION_MAP.put("서울특별시_서대문구", new Coordinate(37.5791, 126.9368));
        LOCATION_MAP.put("서울특별시_서초구", new Coordinate(37.4837, 127.0324));
        LOCATION_MAP.put("서울특별시_성동구", new Coordinate(37.5635, 127.0369));
        LOCATION_MAP.put("서울특별시_성북구", new Coordinate(37.5894, 127.0167));
        LOCATION_MAP.put("서울특별시_송파구", new Coordinate(37.5145, 127.1066));
        LOCATION_MAP.put("서울특별시_양천구", new Coordinate(37.5169, 126.8665));
        LOCATION_MAP.put("서울특별시_영등포구", new Coordinate(37.5264, 126.8963));
        LOCATION_MAP.put("서울특별시_용산구", new Coordinate(37.5326, 126.9900));
        LOCATION_MAP.put("서울특별시_은평구", new Coordinate(37.6027, 126.9291));
        LOCATION_MAP.put("서울특별시_종로구", new Coordinate(37.5730, 126.9794));
        LOCATION_MAP.put("서울특별시_중구", new Coordinate(37.5636, 126.9975));
        LOCATION_MAP.put("서울특별시_중랑구", new Coordinate(37.6066, 127.0927));

        // ==========================================
        // 2. 부산광역시 (16개 구/군)
        // ==========================================
        LOCATION_MAP.put("부산광역시_강서구", new Coordinate(35.2122, 128.9806));
        LOCATION_MAP.put("부산광역시_금정구", new Coordinate(35.2430, 129.0921));
        LOCATION_MAP.put("부산광역시_기장군", new Coordinate(35.2447, 129.2223));
        LOCATION_MAP.put("부산광역시_남구", new Coordinate(35.1365, 129.0843));
        LOCATION_MAP.put("부산광역시_동구", new Coordinate(35.1293, 129.0454));
        LOCATION_MAP.put("부산광역시_동래구", new Coordinate(35.2048, 129.0838));
        LOCATION_MAP.put("부산광역시_진구", new Coordinate(35.1553, 129.0592));
        LOCATION_MAP.put("부산광역시_부산진구", new Coordinate(35.1553, 129.0592));
        LOCATION_MAP.put("부산광역시_북구", new Coordinate(35.1971, 128.9904));
        LOCATION_MAP.put("부산광역시_사상구", new Coordinate(35.1529, 128.9913));
        LOCATION_MAP.put("부산광역시_사하구", new Coordinate(35.1044, 128.9749));
        LOCATION_MAP.put("부산광역시_서구", new Coordinate(35.0979, 129.0244));
        LOCATION_MAP.put("부산광역시_수영구", new Coordinate(35.1456, 129.1132));
        LOCATION_MAP.put("부산광역시_연제구", new Coordinate(35.1765, 129.0797));
        LOCATION_MAP.put("부산광역시_영도구", new Coordinate(35.0912, 129.0679));
        LOCATION_MAP.put("부산광역시_중구", new Coordinate(35.1062, 129.0324));
        LOCATION_MAP.put("부산광역시_해운대구", new Coordinate(35.1631, 129.1636));

        // ==========================================
        // 3. 대구광역시 (9개 구/군)
        // ==========================================
        LOCATION_MAP.put("대구광역시_남구", new Coordinate(35.8460, 128.5978));
        LOCATION_MAP.put("대구광역시_달서구", new Coordinate(35.8299, 128.5326));
        LOCATION_MAP.put("대구광역시_달성군", new Coordinate(35.7742, 128.4314));
        LOCATION_MAP.put("대구광역시_동구", new Coordinate(35.8863, 128.6355));
        LOCATION_MAP.put("대구광역시_북구", new Coordinate(35.8858, 128.5828));
        LOCATION_MAP.put("대구광역시_서구", new Coordinate(35.8717, 128.5592));
        LOCATION_MAP.put("대구광역시_수성구", new Coordinate(35.8581, 128.6306));
        LOCATION_MAP.put("대구광역시_중구", new Coordinate(35.8714, 128.6014));
        LOCATION_MAP.put("대구광역시_군위군", new Coordinate(36.2428, 128.5728));

        // ==========================================
        // 4. 인천광역시 (10개 구/군)
        // ==========================================
        LOCATION_MAP.put("인천광역시_강화군", new Coordinate(37.7465, 126.4880));
        LOCATION_MAP.put("인천광역시_계양구", new Coordinate(37.5374, 126.7377));
        LOCATION_MAP.put("인천광역시_남동구", new Coordinate(37.4475, 126.7314));
        LOCATION_MAP.put("인천광역시_동구", new Coordinate(37.4738, 126.6432));
        LOCATION_MAP.put("인천광역시_미추홀구", new Coordinate(37.4636, 126.6502));
        LOCATION_MAP.put("인천광역시_부평구", new Coordinate(37.5071, 126.7218));
        LOCATION_MAP.put("인천광역시_서구", new Coordinate(37.5454, 126.6760));
        LOCATION_MAP.put("인천광역시_연수구", new Coordinate(37.4101, 126.6782));
        LOCATION_MAP.put("인천광역시_옹진군", new Coordinate(37.4665, 126.6366));
        LOCATION_MAP.put("인천광역시_중구", new Coordinate(37.4738, 126.6216));

        // ==========================================
        // 5. 광주광역시 (5개 자치구)
        // ==========================================
        LOCATION_MAP.put("광주광역시_광산구", new Coordinate(35.1395, 126.7937));
        LOCATION_MAP.put("광주광역시_남구", new Coordinate(35.1329, 126.9025));
        LOCATION_MAP.put("광주광역시_동구", new Coordinate(35.1460, 126.9230));
        LOCATION_MAP.put("광주광역시_북구", new Coordinate(35.1741, 126.9122));
        LOCATION_MAP.put("광주광역시_서구", new Coordinate(35.1520, 126.8898));
    }

    public static Coordinate getCoordinate(String city, String district) {
        String formattedCity = (city != null) ? city.trim() : "";
        String formattedDistrict = (district != null) ? district.trim() : "";

        // 줄임 표현(서울, 부산 등) 표준 명칭 변환
        if (formattedCity.equals("서울")) formattedCity = "서울특별시";
        if (formattedCity.equals("부산")) formattedCity = "부산광역시";
        if (formattedCity.equals("대구")) formattedCity = "대구광역시";
        if (formattedCity.equals("인천")) formattedCity = "인천광역시";
        if (formattedCity.equals("광주")) formattedCity = "광주광역시";

        String key = formattedCity + "_" + formattedDistrict;
        Coordinate coordinate = LOCATION_MAP.get(key);

        // 예외 시 기본 좌표(서울특별시 중구) 반환
        if (coordinate == null) {
            return new Coordinate(37.5636, 126.9975);
        }
        return coordinate;
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {
        private double lat;
        private double lon;
    }
}
