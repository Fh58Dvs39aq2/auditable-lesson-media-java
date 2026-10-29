package edu.streaming;

import java.util.LinkedHashMap;
import java.util.Map;

/** A small teaching-domain workflow: ingest, process, then deliver a lesson asset. */
public final class MediaDeliveryService {
    private final InfraiClient client;

    public MediaDeliveryService(InfraiClient client) {
        this.client = client;
    }

    public DeliveryReceipt deliver(LessonAsset asset) {
        String jobId = "job-" + asset.assetId();
        String eventId = "event-" + asset.assetId();
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("message", "Delivered lesson asset " + asset.assetId());
        log.put("level", "info");
        log.put("event_id", eventId);
        log.put("idempotency_key", eventId);
        log.put("asset_id", asset.assetId());
        log.put("course_id", asset.courseId());
        log.put("creator_id", asset.creatorId());
        log.put("job_id", jobId);
        log.put("stage", "delivered");
        log.put("lesson_title", asset.lessonTitle());
        client.ingestLog(log);
        return new DeliveryReceipt(asset.assetId(), jobId, eventId, client.search("asset_id:" + asset.assetId()));
    }

    public static LessonAsset exampleAsset() {
        return new LessonAsset("asset-lesson-07", "course-java-foundations", "creator-lee", "Streams and buffers");
    }

    public record LessonAsset(String assetId, String courseId, String creatorId, String lessonTitle) {}
    public record DeliveryReceipt(String assetId, String jobId, String eventId, String searchResult) {}
}
