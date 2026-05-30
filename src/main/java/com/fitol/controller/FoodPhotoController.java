package com.fitol.controller;

import com.fitol.model.FoodLog;
import com.fitol.model.User;
import com.fitol.repository.FoodLogRepository;
import com.fitol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/food/photo")
public class FoodPhotoController {

    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api.key:}")
    private String groqApiKey;

    public FoodPhotoController(FoodLogRepository foodLogRepository, UserRepository userRepository) {
        this.foodLogRepository = foodLogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("hasApiKey", groqApiKey != null && !groqApiKey.isBlank());
        return "food/photo";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public Map<String, Object> analyze(@RequestParam("photo") MultipartFile photo) {
        Map<String, Object> result = new HashMap<>();

        if (photo.isEmpty()) {
            result.put("success", false);
            result.put("message", "Fotoğraf yüklenmedi");
            return result;
        }

        if (groqApiKey == null || groqApiKey.isBlank()) {
            result.put("success", false);
            result.put("message", "API anahtarı yapılandırılmamış. application.properties dosyasına groq.api.key ekleyin.");
            return result;
        }

        try {
            // Fotoğrafı base64'e donustur
            byte[] imageBytes = photo.getBytes();
            String imageData = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = photo.getContentType() != null ? photo.getContentType() : "image/jpeg";

            // Groq API'ye gonder
            String prompt = """
                Sen profesyonel bir diyetisyen ve besin uzmanısın. Bu fotoğraftaki TÜM yemekleri dikkatlice analiz et.
                
                Her yemek için gerçekçi porsiyon tahmini yap ve besin değerlerini hesapla. Türk mutfağını iyi biliyorsun.
                
                Yanıtını SADECE aşağıdaki JSON formatında ver, başka hiçbir şey yazma:
                
                {
                  "foods": [
                    {
                      "name": "Yemek adı (Türkçe)",
                      "portion": "Tahmini porsiyon açıklaması",
                      "grams": tahmini gram (sayı),
                      "calories": kalori (sayı),
                      "protein": protein gram (sayı, 1 ondalık),
                      "carbs": karbonhidrat gram (sayı, 1 ondalık),
                      "fat": yağ gram (sayı, 1 ondalık),
                      "fiber": lif gram (sayı, 1 ondalık),
                      "confidence": güven yüzdesi 0-100 arası (sayı)
                    }
                  ],
                  "total_calories": toplam kalori (sayı),
                  "total_protein": toplam protein (sayı, 1 ondalık),
                  "total_carbs": toplam karbonhidrat (sayı, 1 ondalık),
                  "total_fat": toplam yağ (sayı, 1 ondalık),
                  "description": "Tabağın kısa açıklaması (Türkçe, 1 cümle)",
                  "health_score": sağlık puanı 1-10 (sayı),
                  "tips": "Kısa beslenme önerisi (Türkçe, 1 cümle)"
                }""";

            // Request body olustur
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");
            requestBody.put("max_tokens", 1500);
            requestBody.put("temperature", 0.2);

            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", prompt);

            Map<String, Object> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:" + mimeType + ";base64," + imageData);

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", imageUrl);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", List.of(textContent, imageContent));

            requestBody.put("messages", List.of(message));

            // HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API cagrisi
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.containsKey("error")) {
                result.put("success", false);
                result.put("message", "API hatası oluştu");
                return result;
            }

            // Yaniti parse et
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> msgMap = (Map<String, Object>) choices.get(0).get("message");
            String text = (String) msgMap.get("content");

            // JSON temizle (``` bloklari kaldir)
            text = text.trim();
            if (text.startsWith("```")) {
                String[] lines = text.split("\n");
                text = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length));
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            // JSON parse
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> foodData = mapper.readValue(text, Map.class);
            foodData.put("success", true);
            return foodData;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Hata: " + e.getMessage());
            return result;
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> add(@AuthenticationPrincipal UserDetails ud, @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();

        List<Map<String, Object>> foods = (List<Map<String, Object>>) data.get("foods");
        String mealType = (String) data.getOrDefault("meal_type", "ogle");
        List<String> added = new ArrayList<>();

        for (Map<String, Object> food : foods) {
            FoodLog entry = new FoodLog();
            entry.setUserId(user.getId());
            entry.setDate(LocalDate.now());
            entry.setMealType(mealType);
            entry.setFoodName((String) food.getOrDefault("name", "Bilinmeyen"));
            entry.setPortion(1.0);
            entry.setCalories(toDouble(food.get("calories")));
            entry.setProtein(toDouble(food.get("protein")));
            entry.setCarbs(toDouble(food.get("carbs")));
            entry.setFat(toDouble(food.get("fat")));
            foodLogRepository.save(entry);
            added.add(entry.getFoodName());
        }

        result.put("success", true);
        result.put("message", added.size() + " yemek eklendi: " + String.join(", ", added));
        return result;
    }

    private double toDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) try { return Double.parseDouble((String) val); } catch (Exception e) {}
        return 0;
    }
}
