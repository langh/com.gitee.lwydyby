package com.gdtopway.operatelog.service;

public interface ILogsService<T,S> {
    T selectLogsById(S id,S dataSource);
}
