package com.grace.recon.naas.controller;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.queue.NaasDataQueue;
import com.grace.recon.naas.service.EtlService;
import com.grace.recon.naas.service.ValidationService;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/naas")
public class NaasController {

  private static final Logger LOGGER = Logger.getLogger(NaasController.class.getName());

  private final EtlService etlService;
  private final ValidationService validationService;
  private final NaasDataQueue dataQueue;

  @Autowired
  public NaasController(
      EtlService etlService, ValidationService validationService, NaasDataQueue dataQueue) {
    this.etlService = etlService;
    this.validationService = validationService;
    this.dataQueue = dataQueue;
  }

  @PostMapping("/trigger")
  public ResponseEntity<String> triggerFileIngestion(@RequestBody Map<String, String> payload) {
    String fileName = payload.getOrDefault("fileName", "Unknown File");
    LOGGER.info("Received trigger for file ingestion: " + fileName);

    String lowerCaseFileName = fileName.toLowerCase();

    if (lowerCaseFileName.contains("visa")) {
      List<VisaRecordDto> parsedRecords = etlService.parseVisaFile(fileName);
      List<VisaRecordDto> validRecords = validationService.validateVisaRecords(parsedRecords);
      dataQueue.validationToNormalizationQueue.addAll(
          validRecords); // Add valid records to the queue
      LOGGER.info(
          "VISA file processing complete. Added "
              + validRecords.size()
              + " valid records to the queue.");
    } else if (lowerCaseFileName.contains("switch")) {
      List<SwitchRecordDto> parsedRecords = etlService.parseSwitchFile(fileName);
      List<SwitchRecordDto> validRecords = validationService.validateSwitchRecords(parsedRecords);
      dataQueue.validationToNormalizationQueue.addAll(
          validRecords); // Add valid records to the queue
      LOGGER.info(
          "Switch file processing complete. Added "
              + validRecords.size()
              + " valid records to the queue.");
    } else {
      LOGGER.warning("Unknown file type for: " + fileName);
      return ResponseEntity.badRequest().body("Unknown file type.");
    }

    return ResponseEntity.ok("Successfully queued records for ingestion from file: " + fileName);
  }
}
