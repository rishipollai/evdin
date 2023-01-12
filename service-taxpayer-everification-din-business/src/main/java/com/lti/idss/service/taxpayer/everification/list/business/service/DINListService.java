package com.lti.idss.service.taxpayer.everification.list.business.service;

import com.lti.idss.service.taxpayer.everification.din.common.dto.DINCatDTO;
import com.lti.idss.service.taxpayer.everification.din.common.dto.DINListDTO;
import com.lti.idss.service.taxpayer.everification.din.common.dto.DINListExposureDTO;
import com.lti.idss.service.taxpayer.everification.din.common.request.DINList;
import com.lti.idss.service.taxpayer.everification.din.common.response.*;
import com.lti.idss.service.taxpayer.everification.din.common.utility.AppUtility;
import com.lti.idss.service.taxpayer.everification.din.common.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DINListService {

  private final Logger Logger = LoggerFactory.getLogger(DINListService.class);

  @Autowired
  private RestTemplate restTemplate;

  @Value("${e-verification-exposure}")
  private String eVerificationExposureService;

  public DINListResponse getDINList(DINList request) {
    DINListExposureDTO data = getDINListExposure(getURL(eVerificationExposureService, Constants.LIST_EXPOSURE), request);
    Logger.info("Data: " + data);
    List<DINListBusinessResponse> response = buildDINListResponse(data, request);
    Logger.info("Response: " + response);
    return new DINListResponse(response);
  }

  /**
   * @param data
   * @param request
   * @return - A list of DIN
   */
  private List<DINListBusinessResponse> buildDINListResponse(DINListExposureDTO data, DINList request) {
    return data.getOriginal().stream().map(item -> {
      DINListBusinessResponse response = new DINListBusinessResponse();
      response.setEvSeqId(item.getEvSeqId());
      response.setEvTypeId(item.getEvTypeId());
      response.setDin(item.getDin());
      response.setType(item.getEvTypeDesc());
      response.setDescription(item.getEvSubTypeDesc());
      response.setIssuedOn(AppUtility.formatDate(item.getDinIssueDate()));
      response.setResponseDueDate(AppUtility.formatDate(item.getRespDueDate()));
      response.setEmail(item.getEmail());
      response.setMobile(item.getMobile());
      response.setReadFlag(checkReadFlag(item, data.getReminder()));
      response.setTotalReminder(countTotalReminder(item, data.getReminder()));
      response.setReminder(getReminderDetails(item, data.getReminder()));
      response.setCategoryDetails(getCategoryDetails(data.getCategoryData(), item));
      response.setOverallStatus(getOverallStatus(response.getCategoryDetails()));
      return response;
    }).collect(Collectors.toList());
  }

  /**
   *
   * @param categoryDetails
   * @return - Overall Status for a DIN
   */
  private String getOverallStatus(List<CategoryDetails> categoryDetails) {
    List<String> questionStatus = categoryDetails.stream().map(CategoryDetails::getQuestionsStatus).collect(Collectors.toList());
    List<String> transactionStatus = categoryDetails.stream().map(CategoryDetails::getTransactionsStatus).collect(Collectors.toList());
    if (questionStatus.contains(Constants.STATUS_PENDING) || transactionStatus.contains(Constants.STATUS_PENDING)) {
      return Constants.STATUS_PENDING;
    }
    return Constants.STATUS_SUBMITTED;
  }

  /**
   *
   * @param categoryData
   * @param item
   * @return - Details of categories selected for DIN
   */
  private List<CategoryDetails> getCategoryDetails(List<DINCatDTO> categoryData, DINListDTO item) {
    List<CategoryDetails> response = new ArrayList<>();
    for (DINCatDTO data : categoryData) {
      if (Objects.equals(item.getEvSeqId(), data.getEvSeqId())) {
        CategoryDetails categoryDetails = new CategoryDetails();
        categoryDetails.setInformationCategory(data.getCatName());
        categoryDetails.setProcessedValue(data.getProcessedValue());
        categoryDetails.setDisclosedValue(data.getDisclosedValue());
        categoryDetails.setQuestionsStatus(getQuestionStatus(data.getQuestionStatus()));
        categoryDetails.setTransactionsStatus(getTransactionStatus(data.getTransactionStatus()));
        categoryDetails.setInfoCatCode(data.getCatCode());
        response.add(categoryDetails);
      }
    }
    return response;
  }

  /**
   *
   * @param transactionStatus
   * @return - Set Transaction Status
   */
  private String getTransactionStatus(String transactionStatus) {
    if (transactionStatus.equalsIgnoreCase(Constants.STATUS_CODE_VERIFIED)) {
      return Constants.STATUS_VERIFIED;
    } else if (transactionStatus.equalsIgnoreCase(Constants.STATUS_CODE_PENDING)) {
      return Constants.STATUS_PENDING;
    } else {
      return Constants.STATUS_VIEW;
    }
  }

  /**
   *
   * @param status
   * @return - Question Status
   */
  private String getQuestionStatus(String status) {
    if (status.equalsIgnoreCase(Constants.STATUS_CODE_SUBMITTED)) {
      return Constants.STATUS_SUBMITTED;
    }
    return Constants.STATUS_PENDING;
  }

  /**
   *
   * @param item
   * @param reminders
   * @return - A List of reminders
   */
  private List<ReminderResponse> getReminderDetails(DINListDTO item, List<DINListDTO> reminders) {
    return reminders.stream().filter(x -> x.getDin().contains(item.getDin())).map(reminder -> {
      ReminderResponse response = new ReminderResponse();
      response.setLinkedDIN(reminder.getDin());
      response.setDescription(reminder.getEvSubTypeDesc() + " - Reminder");
      response.setEmail(reminder.getEmail());
      response.setMobile(reminder.getMobile());
      response.setIssuedOn(AppUtility.formatDate(reminder.getDinIssueDate()));
      return response;
    }).collect(Collectors.toList());
  }

  /**
   *
   * @param item
   * @param reminders
   * @return - A Boolean value whether the notifications have been seen or not
   */
  private Boolean checkReadFlag(DINListDTO item, List<DINListDTO> reminders) {
    List<DINListDTO> filterData = reminders.stream()
        .filter(x -> x.getDin().contains(item.getDin()))
        .sorted(Comparator.comparingInt(DINListDTO::getReminderCount))
        .collect(Collectors.toList());
    if (filterData.size() == 0) {
      return AppUtility.getReadFlag('Y');
    }
    DINListDTO data = filterData.get(filterData.size() - 1);
    return AppUtility.getReadFlag(data.getReadFlag());
  }

  /**
   *
   * @param item
   * @param reminders
   * @return - Total Number of Reminder
   */
  private Integer countTotalReminder(DINListDTO item, List<DINListDTO> reminders) {
    int count = 0;
    for (DINListDTO reminder : reminders) {
      if (reminder.getDin().contains(item.getDin())) {
        count += 1;
      }
    }
    return count;
  }

  /**
   *
   * @param request(pan,financialYear)
   * @return - A list of DIN with other required details
   */
  private DINListExposureDTO getDINListExposure(String url, DINList request) {
    return restTemplate.postForEntity(url, request, DINListExposureDTO.class).getBody();
  }

  /**
   *
   * @param base
   * @param endpoint
   * @return - URL
   */
  private String getURL(String base, String endpoint) {
    return base + endpoint;
  }

}
