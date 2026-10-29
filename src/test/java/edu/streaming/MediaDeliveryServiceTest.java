package edu.streaming;

import java.net.http.HttpClient;

public final class MediaDeliveryServiceTest {
    public static void main(String[] args) {
        MediaDeliveryService.LessonAsset asset = MediaDeliveryService.exampleAsset();
        if (!"course-java-foundations".equals(asset.courseId())) throw new AssertionError("course must be retained");
        if (!"creator-lee".equals(asset.creatorId())) throw new AssertionError("creator must be retained");
        if (!"asset-lesson-07".equals(asset.assetId())) throw new AssertionError("asset id must be stable");
        System.out.println("business decision test passed: one delivered lesson keeps course and creator identity");
    }
}
