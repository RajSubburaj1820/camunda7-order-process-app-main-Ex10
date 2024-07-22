package com.camunda.training.order;

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
@ExternalTaskSubscription("paymentRequest")
public class SendMessageHandler implements ExternalTaskHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SendMessageHandler.class);

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    LOG.info("handle topic {} for task id {}", externalTask.getTopicName(), externalTask.getId());

    ApiClient client = new ApiClient();
    String paymentProcessInstanceId = null;

    Map<String, VariableValueDto> payload = new HashMap<>();
    for (Map.Entry<String, Object> variable : externalTask.getAllVariables().entrySet()) {
      payload.put(variable.getKey(), new VariableValueDto().value(variable.getValue()));
    }

    try {
      List<MessageCorrelationResultWithVariableDto> correlationResult =
          new MessageApi(client).deliverMessage(new CorrelationMessageDto()
              .messageName("paymentRequestMessage").businessKey(externalTask.getBusinessKey())
              .processVariables(payload).resultEnabled(true));
      LOG.info("correlation result: {}", correlationResult);
      paymentProcessInstanceId = correlationResult.get(0).getProcessInstance().getId();
    } catch (ApiException e) {
      e.printStackTrace();
    }

    externalTaskService.complete(externalTask,
        Map.of("paymentProcessInstanceId", paymentProcessInstanceId));

  }
}
