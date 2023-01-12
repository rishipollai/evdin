package com.lti.idss.service.taxpayer.everification.list.business.controller;

import com.lti.idss.service.taxpayer.everification.din.common.request.DINList;
import com.lti.idss.service.taxpayer.everification.din.common.response.DINListResponse;
import com.lti.idss.service.taxpayer.everification.list.business.service.DINListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DINListController {

  private final Logger Logger = LoggerFactory.getLogger(DINListController.class);

  @Autowired
  private DINListService service;

  @PostMapping(value = "/list", produces = "application/json", consumes = "application/json")
  public ResponseEntity<DINListResponse> getDINList(@Valid @RequestBody DINList request) {
    Logger.info("Request: " + request);
    return new ResponseEntity<>(service.getDINList(request), HttpStatus.OK);
  }
}
