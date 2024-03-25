package consumer.dbclient;

import consumer.LiftRide;
import java.util.HashMap;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public class DDBClient {
  private static final String TABLE = "skier_table";
  private DynamoDbClient dynamoDbClient;

  public DDBClient() {
    dynamoDbClient = DynamoDbClient.builder()
        .region(Region.US_WEST_2)
        .build();
  }

  public void PutItem(LiftRide liftRide) {
    HashMap<String, AttributeValue> itemValues = new HashMap<>();
    itemValues.put("id", AttributeValue.builder().s(liftRide.getId()).build());
    itemValues.put("seasonId", AttributeValue.builder().s(liftRide.getSeasonId()).build());
    itemValues.put("dayId", AttributeValue.builder().s(liftRide.getDayId()).build());
    itemValues.put("liftID", AttributeValue.builder().n(String.valueOf(liftRide.getLiftID())).build());
    itemValues.put("skierId", AttributeValue.builder().n(String.valueOf(liftRide.getSkierId())).build());
    itemValues.put("time", AttributeValue.builder().n(String.valueOf(liftRide.getTime())).build());
    itemValues.put("resortId", AttributeValue.builder().n(String.valueOf(liftRide.getResortId())).build());

    PutItemRequest request = PutItemRequest.builder()
        .tableName(TABLE)
        .item(itemValues)
        .conditionExpression("attribute_not_exists(id)")
        .build();

    try {
      dynamoDbClient.putItem(request);
      System.out.println(TABLE + " was successfully updated.");
    } catch (ResourceNotFoundException e) {
      System.out.println("Error: The Amazon DynamoDB table" + TABLE + "can't be found.");
    } catch (DynamoDbException e) {
      if (e.getMessage().contains("The conditional request failed")) {
          UpdateItem(liftRide);
      } else {
        System.out.println("[DBError]" + e.getMessage());
      }
    } catch (Exception e) {
      System.out.println("[DBError]" + e.getMessage());
    }
  }

  public void UpdateItem(LiftRide liftRide) {
    HashMap<String, AttributeValue> itemKey = new HashMap<>();
    itemKey.put("id", AttributeValue.builder()
        .s(liftRide.getId())
        .build());

    HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();
    updatedValues.put("time", AttributeValueUpdate.builder()
        .value(AttributeValue.builder().n(String.valueOf(liftRide.getTime())).build())
        .action(AttributeAction.ADD)
        .build());

    UpdateItemRequest request = UpdateItemRequest.builder()
        .tableName(TABLE)
        .key(itemKey)
        .attributeUpdates(updatedValues)
        .build();

    try {
      dynamoDbClient.updateItem(request);
      System.out.println("Update item " + liftRide.getId() + " succeeded");
    } catch (Exception e) {
      System.out.println("[DBError]" + e.getMessage());
    }
  }
}
