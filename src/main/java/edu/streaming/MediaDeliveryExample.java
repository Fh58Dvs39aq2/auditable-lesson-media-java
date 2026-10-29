package edu.streaming;

import java.net.http.HttpClient;

public final class MediaDeliveryExample {
    private MediaDeliveryExample() {}

    public static void main(String[] args) {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        InfraiClient client = new InfraiClient(HttpClient.newHttpClient(), key);
        String identity = client.whoAmI();
        MediaDeliveryService.DeliveryReceipt receipt = new MediaDeliveryService(client)
                .deliver(MediaDeliveryService.exampleAsset());
        System.out.println("credential=" + identity);
        System.out.println("delivered asset=" + receipt.assetId() + " job=" + receipt.jobId());
        System.out.println("search envelope=" + receipt.searchResult());
    }
}
