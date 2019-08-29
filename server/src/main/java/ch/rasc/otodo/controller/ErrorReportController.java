package ch.rasc.otodo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rasc.otodo.Application;
import ch.rasc.otodo.dto.ClientError;
import ch.rasc.otodo.dto.CspReport;

@RestController
@RequestMapping("/be")
class ErrorReportController {

  @PostMapping("/client-error")
  public void clientError(@RequestBody List<ClientError> errors) {
    for (ClientError error : errors) {
      Application.log.error(error.toString());
    }
  }

  @PostMapping("/csp-error")
  public void cspError(@RequestBody CspReport cspReport) {
    Application.log.error(cspReport.toString());
  }

}
