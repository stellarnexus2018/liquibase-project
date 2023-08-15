package ru.master.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BasicService {
  @Scheduled(fixedRate = 10000)
  public void test() {

    try {

    } catch (Exception e) {

    }
  }
}
