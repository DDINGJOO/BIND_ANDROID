package com.teambind.bind_android.data.model

/**
 * 장르 목록
 */
enum class Genre(val code: Int, val displayName: String, val serverKey: String) {
    ROCK(1, "록", "ROCK"),
    POP(2, "팝", "POP"),
    JAZZ(3, "재즈", "JAZZ"),
    CLASSICAL(4, "클래식", "CLASSICAL"),
    HIP_HOP(5, "힙합", "HIP_HOP"),
    ELECTRONIC(6, "전자음악", "ELECTRONIC"),
    FOLK(7, "포크", "FOLK"),
    BLUES(8, "블루스", "BLUES"),
    REGGAE(9, "레게", "REGGAE"),
    METAL(10, "메탈", "METAL"),
    COUNTRY(11, "컨트리", "COUNTRY"),
    LATIN(12, "라틴", "LATIN"),
    RNB(13, "R&B", "RNB"),
    SOUL(14, "소울", "SOUL"),
    FUNK(15, "펑크(Funk)", "FUNK"),
    PUNK(16, "펑크(Punk)", "PUNK"),
    ALTERNATIVE(17, "얼터너티브", "ALTERNATIVE"),
    INDIE(18, "인디", "INDIE"),
    GOSPEL(19, "가스펠", "GOSPEL"),
    OPERA(20, "오페라", "OPERA"),
    SOUNDTRACK(21, "사운드트랙", "SOUNDTRACK"),
    WORLD_MUSIC(22, "월드뮤직", "WORLD_MUSIC"),
    OTHER(23, "기타", "OTHER");

    companion object {
        fun fromCode(code: Int): Genre? = entries.find { it.code == code }
        fun fromServerKey(serverKey: String): Genre? = entries.find { it.serverKey == serverKey }
        fun getCodesFromServerKeys(serverKeys: List<String>): List<Int> =
            serverKeys.mapNotNull { fromServerKey(it)?.code }
        fun getServerKeysFromCodes(codes: List<Int>): List<String> =
            codes.mapNotNull { fromCode(it)?.serverKey }
    }
}

/**
 * 악기 목록
 */
enum class Instrument(val code: Int, val displayName: String, val serverKey: String) {
    VOCAL(1, "보컬", "VOCAL"),
    GUITAR(2, "기타", "GUITAR"),
    BASS(3, "베이스", "BASS"),
    DRUM(4, "드럼", "DRUM"),
    KEYBOARD(5, "키보드", "KEYBOARD"),
    PERCUSSION(6, "퍼커션", "PERCUSSION"),
    SAXOPHONE(7, "색소폰", "SAXOPHONE"),
    VIOLIN(8, "바이올린", "VIOLIN"),
    CELLO(9, "첼로", "CELLO"),
    TRUMPET(10, "트럼펫", "TRUMPET"),
    FLUTE(11, "플루트", "FLUTE"),
    DJ(12, "DJ", "DJ"),
    PRODUCER(13, "프로듀서", "PRODUCER"),
    ETC(14, "기타", "ETC");

    companion object {
        fun fromCode(code: Int): Instrument? = entries.find { it.code == code }
        fun fromServerKey(serverKey: String): Instrument? = entries.find { it.serverKey == serverKey }
        fun getCodesFromServerKeys(serverKeys: List<String>): List<Int> =
            serverKeys.mapNotNull { fromServerKey(it)?.code }
        fun getServerKeysFromCodes(codes: List<Int>): List<String> =
            codes.mapNotNull { fromCode(it)?.serverKey }
    }
}

/**
 * 지역 목록
 */
enum class Region(val code: String, val displayName: String) {
    SEOUL("SEOUL", "서울"),
    GYEONGGI("GYEONGGI", "경기"),
    INCHEON("INCHEON", "인천"),
    BUSAN("BUSAN", "부산"),
    DAEGU("DAEGU", "대구"),
    DAEJEON("DAEJEON", "대전"),
    GWANGJU("GWANGJU", "광주"),
    ULSAN("ULSAN", "울산"),
    SEJONG("SEJONG", "세종"),
    GANGWON("GANGWON", "강원"),
    CHUNGBUK("CHUNGBUK", "충북"),
    CHUNGNAM("CHUNGNAM", "충남"),
    JEONBUK("JEONBUK", "전북"),
    JEONNAM("JEONNAM", "전남"),
    GYEONGBUK("GYEONGBUK", "경북"),
    GYEONGNAM("GYEONGNAM", "경남"),
    JEJU("JEJU", "제주"),
    ETC("ETC", "기타");

    companion object {
        fun fromCode(code: String): Region? = entries.find { it.code == code }
        fun fromDisplayName(name: String): Region? = entries.find { it.displayName == name }
        fun getCodeFromDisplayName(name: String): String = fromDisplayName(name)?.code ?: "ETC"
        fun getDisplayNameFromCode(code: String): String = fromCode(code)?.displayName ?: "기타"

        val allDisplayNames: List<String> = entries.map { it.displayName }
    }
}
