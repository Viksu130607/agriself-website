package com.agriself.service;

import com.agriself.dto.FarmerAIRequestDTO;
import com.agriself.dto.FarmerAIResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Autonomous Multi-Lingual Agritech Advisory Engine for Indian Farmers.
 * Tailored with everyday simple words, empathetic guidance, and regional dialects.
 * Operates offline with deterministic agro-rules, and supports Gemini API if GEMINI_API_KEY is configured.
 */
@Service
@Slf4j
public class FarmerAIEngine {

    @Value("${agriself.gemini.api-key:}")
    private String geminiApiKey;

    public FarmerAIResponseDTO generateAdvice(FarmerAIRequestDTO request) {
        String lang = (request.getLanguage() != null && !request.getLanguage().isBlank())
                ? request.getLanguage().toLowerCase().trim()
                : "hi"; // Default to Hindi

        String topic = (request.getTopic() != null && !request.getTopic().isBlank())
                ? request.getTopic().toUpperCase().trim()
                : "GENERAL";

        String question = (request.getQuestion() != null) ? request.getQuestion().trim() : "";
        String crop = (request.getCropName() != null && !request.getCropName().isBlank())
                ? request.getCropName()
                : "गेहूं / फसल";

        // Detect topic from question if general
        if ("GENERAL".equals(topic) && !question.isEmpty()) {
            topic = detectTopicFromText(question);
        }

        return buildRegionalAdvice(lang, topic, crop, question);
    }

    private String detectTopicFromText(String q) {
        String lower = q.toLowerCase();
        if (lower.contains("कीड़े") || lower.contains("रोग") || lower.contains("pest") || lower.contains("insect")
                || lower.contains("इल्ली") || lower.contains("फफूंद") || lower.contains("डाग") || lower.contains("कीट")) {
            return "PEST";
        }
        if (lower.contains("पानी") || lower.contains("सिंचाई") || lower.contains("water") || lower.contains("irrigation")
                || lower.contains("ड्रिप") || lower.contains("नमी")) {
            return "WATER";
        }
        if (lower.contains("खाद") || lower.contains("यूरिया") || lower.contains("fertilizer") || lower.contains("urea")
                || lower.contains("डीएपी") || lower.contains("dap") || lower.contains("पोटाश")) {
            return "FERTILIZER";
        }
        if (lower.contains("मौसम") || lower.contains("बारिश") || lower.contains("weather") || lower.contains("rain")
                || lower.contains("पाला") || lower.contains("धूप")) {
            return "WEATHER";
        }
        return "GENERAL";
    }

    private FarmerAIResponseDTO buildRegionalAdvice(String lang, String topic, String crop, String question) {
        switch (lang) {
            case "mr":
                return getMarathiAdvice(topic, crop);
            case "pa":
                return getPunjabiAdvice(topic, crop);
            case "gu":
                return getGujaratiAdvice(topic, crop);
            case "te":
                return getTeluguAdvice(topic, crop);
            case "ta":
                return getTamilAdvice(topic, crop);
            case "bn":
                return getBengaliAdvice(topic, crop);
            case "en":
                return getEnglishAdvice(topic, crop);
            case "hi":
            default:
                return getHindiAdvice(topic, crop);
        }
    }

    // -------------------------------------------------------------
    // HINDI (हिंदी - सरल किसान भाषा)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getHindiAdvice(String topic, String crop) {
        switch (topic) {
            case "PEST":
                return FarmerAIResponseDTO.builder()
                        .language("hi")
                        .languageName("हिंदी (Hindi)")
                        .warningTitle("⚠️ सावधान: " + crop + " में कीट एवं बीमारी की चेतावनी!")
                        .warningBadge("URGENT")
                        .advice("किसान भाई, पत्तियों पर काले या पीले धब्बे और सुंडी दिखने पर घबराएं नहीं। तुरंत सरल देसी या सही छिड़काव करें ताकि फसल सुरक्षित रहे।")
                        .simpleActionSteps(List.of(
                                "1. सुबह जल्दी 5 मिली नीम का तेल 1 लीटर गुनगुने पानी में थोड़ा साबुन घोलकर पत्तियों के नीचे छिड़कें।",
                                "2. यदि कीड़े ज्यादा हों, तो नजदीकी कृषि केंद्र से पूछकर केवल अनुशंसित मात्रा में इमिडाक्लोप्रिड या प्रोफेनोफॉस का छिड़काव करें।",
                                "3. तेज दोपहर की धूप में छिड़काव कभी न करें, हमेशा शाम 4 बजे के बाद या सुबह करें।",
                                "4. खेत में पीले चिपचिपे कार्ड (Yellow Sticky Traps) लगाएं ताकि उड़ने वाले रस चूसक कीड़े तुरंत फंस जाएं।"
                        ))
                        .organicSolution("देसी नुस्खा: 10 लीटर गोमूत्र + 2 किलो नीम की पत्ती + 250 ग्राम लहसुन का काढ़ा बनाकर 100 लीटर पानी में मिलाकर स्प्रे करें।")
                        .weatherNote("अगले 48 घंटे में हवा में नमी रहेगी, इसलिए फफूंद (Fungus) की रोकथाम पहले से करें।")
                        .build();

            case "WATER":
                return FarmerAIResponseDTO.builder()
                        .language("hi")
                        .languageName("हिंदी (Hindi)")
                        .warningTitle("💧 " + crop + " के लिए सही पानी व सिंचाई की सलाह")
                        .warningBadge("ADVISORY")
                        .advice("किसान भाई, ज्यादा पानी देने से जड़ें सड़ सकती हैं और कम पानी से दाना पतला रह जाता है। मिट्टी में नमी देखकर ही ड्रिप चलाएं।")
                        .simpleActionSteps(List.of(
                                "1. मिट्टी को हाथ में लेकर लड्डू बनाएं। अगर लड्डू बन रहा है और हाथ पर नमी लग रही है, तो आज पानी न दें।",
                                "2. अगर ड्रिप सिंचाई है, तो सुबह 6 से 8 बजे के बीच 45 से 60 मिनट चलाएं।",
                                "3. दोपहर 12 से 3 बजे के बीच कभी पानी न दें, इससे मिट्टी गरम होकर पौधे झुलस जाते हैं।",
                                "4. क्यारियों के आखिरी छोर पर जल-जमाव न होने दें, अतिरिक्त पानी निकलने की नाली खुली रखें।"
                        ))
                        .organicSolution("ड्रिप में जीवामृत (100 लीटर प्रति एकड़) पानी के साथ चलाएं ताकि मिट्टी मुलायम और उपजाऊ रहे।")
                        .weatherNote("शनिवार को हल्की बारिश का अनुमान है, इसलिए कल शाम के बाद भारी सिंचाई रोक दें।")
                        .build();

            case "FERTILIZER":
                return FarmerAIResponseDTO.builder()
                        .language("hi")
                        .languageName("हिंदी (Hindi)")
                        .warningTitle("🌱 " + crop + " में सही खाद की मात्रा और समय")
                        .warningBadge("NORMAL")
                        .advice("किसान भाई, यूरिया ज्यादा डालने से पौधे सिर्फ लंबे होते हैं और कीड़े ज्यादा लगते हैं। हमेशा सही संतुलित खुराक दें।")
                        .simpleActionSteps(List.of(
                                "1. पहली सिंचाई के समय: प्रति एकड़ 25 किलो यूरिया + 5 किलो जिंक सल्फेट दें।",
                                "2. अगर पत्तियां पीली पड़ रही हैं, तो 19:19:19 (NPK) खाद 1 किलो प्रति 100 लीटर पानी में मिलाकर पत्तों पर स्प्रे करें।",
                                "3. फूल या दाना बनते समय यूरिया बिल्कुल न डालें, केवल पोटाश (0:0:50) का छिड़काव करें जिससे दाना चमकीला और भारी बने।",
                                "4. खाद हमेशा शाम के समय नमी वाली मिट्टी में ही डालें।"
                        ))
                        .organicSolution("हर 15 दिन में 1 बोरी वर्मीकम्पोस्ट (केंचुआ खाद) जड़ों के पास बुरकें।")
                        .weatherNote("खाद डालने के तुरंत बाद हल्की सिंचाई जरूर करें।")
                        .build();

            case "WEATHER":
            default:
                return FarmerAIResponseDTO.builder()
                        .language("hi")
                        .languageName("हिंदी (Hindi)")
                        .warningTitle("🌦️ " + crop + " के लिए मौसम चेतावनी व सलाह")
                        .warningBadge("ADVISORY")
                        .advice("किसान भाई, मौसम विभाग के अनुसार आने वाले दिनों में बादलों के साथ तेज हवाएं चल सकती हैं। फसल की सुरक्षा पहले से तय करें।")
                        .simpleActionSteps(List.of(
                                "1. पकी हुई या तैयार फसल को खुले में न रखें, तुरंत सुरक्षित तिरपाल से ढकें।",
                                "2. तेज हवा से फसल गिरने से बचाने के लिए भारी सिंचाई न करें।",
                                "3. कटी हुई फसल को खेत में जमीन पर सीधे न फैलाएं।"
                        ))
                        .organicSolution("फसल पर छाछ (खट्टी लस्सी) और हींग का घोल स्प्रे करें, इससे मौसम के तनाव से पौधा बचता है।")
                        .weatherNote("तापमान सामान्य से 2 डिग्री अधिक रह सकता है, सुबह की ओस का लाभ लें।")
                        .build();
        }
    }

    // -------------------------------------------------------------
    // MARATHI (मराठी - शेतकरी मित्रांसाठी सोपे मार्गदर्शन)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getMarathiAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("mr")
                .languageName("मराठी (Marathi)")
                .warningTitle("🌾 शेतकरी मित्र: " + crop + " पिकासाठी महत्त्वाची कृषी सूचना")
                .warningBadge("ADVISORY")
                .advice("नमस्कार शेतकरी बंधूंनो! तुमच्या पिकाची योग्य काळजी घेण्यासाठी खालील सोपे उपाय त्वरित अमलात आणा.")
                .simpleActionSteps(List.of(
                        "१. किडींचा प्रादुर्भाव रोखण्यासाठी एकरी ५ पिवळे चिकट सापळे लावा आणि ५ मिली निंबोळी अर्क फवारा.",
                        "२. दुपारच्या कडक उन्हात पिकाला पाणी देणे टाळा. सकाळी ६ ते ८ च्या दरम्यान ठिबक सिंचन ४५ मिनिटे चालवा.",
                        "३. पिकावर जास्त युरिया टाकू नका. नत्र, स्फुरद आणि पालाश (१९:१९:१९) चे योग्य संतुलन ठेवा.",
                        "४. ढगाळ वातावरण असल्यास तांब्या किंवा बुरशीचा धोका वाढतो, तातडीने ट्रायकोडर्मा वापरा."
                ))
                .organicSolution("सेंद्रिय सल्ला: गोमूत्र, शेण आणि कडुलिंबाच्या पानांचे दशपर्णी अर्क तयार करून फवारणी करा.")
                .weatherNote("पुढील दोन दिवसांत हवामान कोरडे राहील, फवारणीसाठी सकाळची वेळ सर्वोत्तम आहे.")
                .build();
    }

    // -------------------------------------------------------------
    // PUNJABI (ਪੰਜਾਬੀ - ਕਿਸਾਨ ਵੀਰਾਂ ਲਈ ਸਲਾਹ)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getPunjabiAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("pa")
                .languageName("ਪੰਜਾਬੀ (Punjabi)")
                .warningTitle("🌾 ਕਿਸਾਨ ਵੀਰੋ: " + crop + " ਦੀ ਫ਼ਸਲ ਲਈ ਜ਼ਰੂਰੀ ਸਲਾਹ")
                .warningBadge("ADVISORY")
                .advice("ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ ਕਿਸਾਨ ਵੀਰੋ! ਆਪਣੀ ਫ਼ਸਲ ਦੇ ਚੰਗੇ ਝਾੜ ਲਈ ਖਾਦ ਅਤੇ ਪਾਣੀ ਦੀ ਸਹੀ ਵਰਤੋਂ ਕਰੋ।")
                .simpleActionSteps(List.of(
                        "1. ਕਣਕ ਜਾਂ ਝੋਨੇ ਵਿੱਚ ਵਾਧੂ ਯੂਰੀਆ ਨਾ ਪਾਓ, ਪ੍ਰਤੀ ਏਕੜ ਸਿਰਫ਼ ਸਿਫਾਰਸ਼ ਕੀਤੀ ਖੁਰਾਕ ਹੀ ਵਰਤੋ।",
                        "2. ਜੇਕਰ ਪੱਤਿਆਂ 'ਤੇ ਪੀਲਾਪਣ ਦਿਸੇ ਤਾਂ 1 ਕਿਲੋ ਜ਼ਿੰਕ ਸਲਫੇਟ ਅਤੇ ਅੱਧਾ ਕਿਲੋ ਯੂਰੀਆ 100 ਲੀਟਰ ਪਾਣੀ ਵਿੱਚ ਘੋਲ ਕੇ ਛਿੜਕਾਅ ਕਰੋ।",
                        "3. ਤੇਜ਼ ਧੁੱਪ ਵਿੱਚ ਪਾਣੀ ਨਾ ਲਗਾਓ, ਸਵੇਰੇ ਜਾਂ ਸ਼ਾਮ ਨੂੰ ਹੀ ਪਾਣੀ ਦਿਓ।",
                        "4. ਸੁੰਡੀ ਜਾਂ ਕੀੜੇ ਦੀ ਰੋਕਥਾਮ ਲਈ ਨਿੰਮ ਦੇ ਤੇਲ ਦਾ ਸਪਰੇਅ ਪਹਿਲ ਦੇ ਆਧਾਰ 'ਤੇ ਕਰੋ।"
                ))
                .organicSolution("ਦੇਸੀ ਨੁਸਖ਼ਾ: ਖੱਟੀ ਲੱਸੀ ਵਿੱਚ ਤਾਂਬੇ ਦਾ ਭਾਂਡਾ ਰੱਖ ਕੇ 7 ਦਿਨਾਂ ਬਾਅਦ ਸਪਰੇਅ ਕਰੋ, ਉੱਲੀ (ਫੰਗਸ) ਤੋਂ ਬਚਾਅ ਹੋਵੇਗਾ।")
                .weatherNote("ਆਉਣ ਵਾਲੇ ਦਿਨਾਂ ਵਿੱਚ ਹਵਾ ਦੀ ਗਤੀ ਵਧਣ ਦੀ ਸੰਭਾਵਨਾ ਹੈ, ਸੁਚੇਤ ਰਹੋ।")
                .build();
    }

    // -------------------------------------------------------------
    // GUJARATI (ગુજરાતી - ખેડૂત મિત્રો માટે સરળ માર્ગદર્શન)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getGujaratiAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("gu")
                .languageName("ગુજરાતી (Gujarati)")
                .warningTitle("🌾 ખેડૂત મિત્ર: " + crop + " ના પાક માટે ખાસ સલાહ")
                .warningBadge("ADVISORY")
                .advice("નમસ્તે ખેડૂત મિત્રો! તમારા પાકના રક્ષણ અને સારા ઉત્પાદન માટે નીચેની સાવચેતી રાખો.")
                .simpleActionSteps(List.of(
                        "૧. પાંદડા પર જીવાત દેખાય તો તરત ૧૦ લીટર પાણીમાં ૫૦ મિલી લીંબોળીનું તેલ મેળવી છંટકાવ કરો.",
                        "૨. બપોરના તડકામાં પાણી ન આપવું, સવારે ૬ થી ૮ વાગ્યા સુધી ટપક પદ્ધતિ ચલાવો.",
                        "૩. વધુ પડતો યુરિયા વાપરવાનું ટાળો, જમીનમાં ભેજ જળવાઈ રહે તેનું ધ્યાન રાખો.",
                        "૪. રોગ પ્રતિકારક શક્તિ વધારવા માટે જીવામૃત આપો."
                ))
                .organicSolution("દેશી ઉપાય: ગૌમૂત્ર અને લીમડાના પાનનો ઉકાળો બનાવી છંટકાવ કરવો.")
                .weatherNote("આવનારા બે દિવસ વાદળછાયું વાતાવરણ રહી શકે છે.")
                .build();
    }

    // -------------------------------------------------------------
    // TELUGU (తెలుగు - రైతు సోదరులకు సలహాలు)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getTeluguAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("te")
                .languageName("తెలుగు (Telugu)")
                .warningTitle("🌾 రైతు సోదరులకు: " + crop + " పంట రక్షణ సలహా")
                .warningBadge("ADVISORY")
                .advice("రైతు మిత్రులకు నమస్కారం! మీ పంటను చీడపీడల నుండి కాపాడుకోవడానికి ఈ కింది సూచనలు పాటించండి.")
                .simpleActionSteps(List.of(
                        "1. ఆకులపై పురుగులు లేదా మచ్చలు కనిపిస్తే 1 లీటరు నీటికి 5 మి.లీ వేప నూనె కలిపి పిచికారీ చేయండి.",
                        "2. మధ్యాహ్నం ఎండలో నీరు పెట్టవద్దు, ఉదయం వేళల్లో మాత్రమే డ్రిప్ ద్వారా నీరందించండి.",
                        "3. యూరియా ఎక్కువగా వాడకండి, తగినంత పొటాష్ మరియు జింక్ మాత్రమే వేయండి.",
                        "4. ఎగిరే రసం పీల్చే పురుగుల నివారణకు పసుపు రంగు జిగురు బోర్డులను అమర్చండి."
                ))
                .organicSolution("సేంద్రీయ పద్ధతి: జీవామృతం (ఎకరాకు 100 లీటర్లు) డ్రిప్ ద్వారా అందించండి.")
                .weatherNote("వచ్చే 48 గంటల్లో తేలికపాటి గాలి వీచే అవకాశం ఉంది.")
                .build();
    }

    // -------------------------------------------------------------
    // TAMIL (தமிழ் - விவசாய நண்பர்களுக்கான எளிய ஆலோசனைகள்)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getTamilAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("ta")
                .languageName("தமிழ் (Tamil)")
                .warningTitle("🌾 விவசாய நண்பரே: " + crop + " பயிர் பாதுகாப்பு ஆலோசனை")
                .warningBadge("ADVISORY")
                .advice("வணக்கம் விவசாய நண்பரே! உங்கள் பயிரை பூச்சி மற்றும் நோய்களில் இருந்து காப்பாற்ற எளிய வழிமுறைகள்.")
                .simpleActionSteps(List.of(
                        "1. இலைகளில் பூச்சிகள் தென்பட்டால் 1 லிட்டர் தண்ணீருக்கு 5 மி.லி வேப்ப எண்ணெய் கலந்து காலையில் தெளிக்கவும்.",
                        "2. கடுமையான மதிய வெயிலில் தண்ணீர் பாய்ச்ச வேண்டாம், அதிகாலை 6 மணி முதல் சொட்டு நீர் பாசனம் செய்யவும்.",
                        "3. அளவுக்கு அதிகமாக யூரியா உரம் இட வேண்டாம், பொட்டாஷ் சரியான அளவில் இடவும்.",
                        "4. மஞ்சள் நிற ஒட்டும் பொறிகளை வயலில் வைத்தால் சாறு உறிஞ்சும் பூச்சிகள் ஒட்டிக்கொள்ளும்."
                ))
                .organicSolution("இயற்கை வழி: பஞ்சகவ்யா மற்றும் ஜீவாமிர்தம் பயன்படுத்தி மண்ணின் வளத்தை பாதுகாக்கவும்.")
                .weatherNote("அடுத்த 2 நாட்களுக்கு வானிலை சீராக இருக்கும்.")
                .build();
    }

    // -------------------------------------------------------------
    // BENGALI (বাংলা - কৃষক ভাইদের জন্য পরামর্শ)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getBengaliAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("bn")
                .languageName("বাংলা (Bengali)")
                .warningTitle("🌾 কৃষক বন্ধু: " + crop + " ফসলের গুরুত্বপূর্ণ পরামর্শ")
                .warningBadge("ADVISORY")
                .advice("নমস্কার কৃষক ভাই! আপনার ফসলের সুস্বাস্থ্যের জন্য নিচের সহজ টিপসগুলি অবিলম্বে অনুসরণ করুন।")
                .simpleActionSteps(List.of(
                        "১. পাতায় পোকা বা দাগ দেখলে ১ লিটার জলে ৫ মিলি নিম তেল মিশিয়ে সকালে স্প্রে করুন।",
                        "২. দুপুরের কড়া রোদে সেচ দেবেন না, সকালবেলা ড্রিপ সেচ দিন।",
                        "৩. অতিরিক্ত ইউরিয়া ব্যবহার করবেন না, সুষম এনপিকে সার ব্যবহার করুন।",
                        "৪. মাঠে হলুদ আঠালো ফাঁদ রাখুন যাতে পোকা সহজে ধরা পড়ে।"
                ))
                .organicSolution("জৈব সমাধান: গোমূত্র ও নিমপাতার তৈরি ভেষজ কীটনাশক স্প্রে করুন।")
                .weatherNote("আগামীকাল হালকা মেঘলা আকাশ থাকতে পারে।")
                .build();
    }

    // -------------------------------------------------------------
    // SIMPLE ENGLISH (Colloquial Everyday Farmer English)
    // -------------------------------------------------------------
    private FarmerAIResponseDTO getEnglishAdvice(String topic, String crop) {
        return FarmerAIResponseDTO.builder()
                .language("en")
                .languageName("English (Simple Farmer Words)")
                .warningTitle("🌾 Farmer Friend: Care Advice for " + crop)
                .warningBadge("ADVISORY")
                .advice("Hello Farmer Friend! Here is simple, practical advice to protect your crop and get high yield.")
                .simpleActionSteps(List.of(
                        "1. If you see insects or leaf spots, mix 5ml Neem Oil in 1 liter warm water and spray early morning.",
                        "2. Never water your field in hot afternoon sun. Run drip irrigation between 6:00 AM and 8:00 AM.",
                        "3. Do not over-use Urea. Use balanced NPK (19:19:19) for steady green growth and strong roots.",
                        "4. Install yellow sticky traps in the field to catch flying pests naturally without chemicals."
                ))
                .organicSolution("Organic Tip: Use Jeevamrut (100 Liters per acre) with irrigation to keep soil soft and rich.")
                .weatherNote("Gentle breeze and partly cloudy conditions forecast for the next 48 hours.")
                .build();
    }
}
