package com.camunda.training.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.community.rest.client.api.MessageApi;
import org.camunda.community.rest.client.dto.CorrelationMessageDto;
import org.camunda.community.rest.client.dto.MessageCorrelationResultWithVariableDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiClient;
import org.camunda.community.rest.client.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("paymentCompletion")
public class PaymentCompletionHandler implements ExternalTaskHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentCompletionHandler.class);

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    LOG.info("handle topic {} for task id {}", externalTask.getTopicName(), externalTask.getId());

    ApiClient client = new ApiClient();

    Map<String, VariableValueDto> correlationKey = new HashMap<>();
    correlationKey.put("paymentProcessInstanceId",
        new VariableValueDto().value(externalTask.getProcessInstanceId()));
    try {
      List<MessageCorrelationResultWithVariableDto> correlationResult = new MessageApi(client)
          .deliverMessage(new CorrelationMessageDto().messageName("paymentCompletedMessage")
              .correlationKeys(correlationKey).resultEnabled(true));
      LOG.info("correlation result: {}", correlationResult);
    } catch (ApiException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    externalTaskService.complete(externalTask);
  }
}
