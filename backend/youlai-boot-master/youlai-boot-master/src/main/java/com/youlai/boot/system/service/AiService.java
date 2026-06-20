package com.youlai.boot.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

/**
 * AI 智能对接服务（纯API调用，无需继承ServiceImpl）
 */
@Service
public class AiService {

    // 自动读取你在 application.yml 里配置的值
    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据访客信息，利用 Moonshot 大模型生成个性化迎接话术建议
     */
    public String generateReceptionScript(String applicantName, String visitorCompany, String visitPurpose) {
        try {
            // 增强系统提示词：要求返回结构化内容
            String systemPrompt = "你是一个专业的企业智能行政助手。你的任务是为内部员工（被访人）生成接待访客的建议。要求：" +
                "1. 输出内容分为【迎接话术】和【接待准备建议】两部分。" +
                "2. 话术要亲切、专业，包含对方姓名和来访事由。" +
                "3. 准备建议要根据事由提供具体的座位、茶水或文档准备建议。";

            String userPrompt = String.format(
                "访客姓名：%s\n来访单位：%s\n来访事由：%s\n\n" +
                    "请严格按照以下格式输出：\n" +
                    "【迎接话术】：[在这里写一段口语化的迎接词]\n" +
                    "【接待准备建议】：[在这里写具体的准备工作，如：准备投影仪/备好纸质方案/更换到会议室B]",
                applicantName, visitorCompany, visitPurpose);

            // 3. 组装符合 OpenAI 标准的请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model); // 👈 这里动态读取了你在 yml 配置的 moonshot-v1-8k

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            // 4. 设置标准请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey); // 👈 自动带上你的 sk-jlW0NV... 密钥

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 5. 使用 RestTemplate 发送 POST 请求
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // 6. 解析大模型返回的固定 JSON 格式
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            // 如果调用超时或报错，打印错误日志，并返回保底的常规文本，确保业务不卡死
            e.printStackTrace();
            return "【AI提示】智能接待建议生成失败，请做好常规接待准备。";
        }
        return "【AI提示】大模型服务繁忙，请做好常规接待准备。";
    }
}
