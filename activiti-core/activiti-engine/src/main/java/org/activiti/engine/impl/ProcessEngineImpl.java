/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl;

import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionContextFactory;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class ProcessEngineImpl implements ProcessEngine {

  private static Logger log = LoggerFactory.getLogger(ProcessEngineImpl.class);

  protected String name;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historicDataService;
  protected TaskService taskService;
  protected ManagementService managementService;
  protected DynamicBpmnService dynamicBpmnService;
  protected AsyncExecutor asyncExecutor;
  protected CommandExecutor commandExecutor;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected TransactionContextFactory transactionContextFactory;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public ProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
      /**
       *  对象的属性值都是从processEngineConfiguration中得到，（门面模式）的设计模式
       */
    this.processEngineConfiguration = processEngineConfiguration;  // 流程引擎配置类实例
    this.name = processEngineConfiguration.getProcessEngineName(); // 流程引擎的名称
      // 初始化各种服务类实例
    this.repositoryService = processEngineConfiguration.getRepositoryService();
    this.runtimeService = processEngineConfiguration.getRuntimeService();
    this.historicDataService = processEngineConfiguration.getHistoryService();
    this.taskService = processEngineConfiguration.getTaskService();
    this.managementService = processEngineConfiguration.getManagementService();
    this.dynamicBpmnService = processEngineConfiguration.getDynamicBpmnService();
    this.asyncExecutor = processEngineConfiguration.getAsyncExecutor(); // 异步作业执行器
    this.commandExecutor = processEngineConfiguration.getCommandExecutor(); // 命令执行器
    this.sessionFactories = processEngineConfiguration.getSessionFactories();
    // 事务上下文工厂类
    this.transactionContextFactory = processEngineConfiguration.getTransactionContextFactory();

    if (processEngineConfiguration.isUsingRelationalDatabase() && processEngineConfiguration.getDatabaseSchemaUpdate() != null) {
        // 执行数据库表生成策略，
      commandExecutor.execute(processEngineConfiguration.getSchemaCommandConfig(), new SchemaOperationsProcessEngineBuild());
    }

    if (name == null) {
      log.info("default activiti ProcessEngine created");
    } else {
      log.info("ProcessEngine {} created", name);
    }
    // 从一下过程：上文提到的流程引擎对象构造完毕。，会将自身的信息注册到流程引擎管理类中去
    ProcessEngines.registerProcessEngine(this);

    if (asyncExecutor != null && asyncExecutor.isAutoActivate()) {
      asyncExecutor.start();
    }

    if (processEngineConfiguration.getProcessEngineLifecycleListener() != null) {
      processEngineConfiguration.getProcessEngineLifecycleListener().onProcessEngineBuilt(this);
    }

    processEngineConfiguration.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createGlobalEvent(ActivitiEventType.ENGINE_CREATED));
  }
    // 流程引擎的注销过程
  public void close() {
    ProcessEngines.unregister(this);
    if (asyncExecutor != null && asyncExecutor.isActive()) {  // 如果流程引擎配置了 异步作业器，则需要关闭作业执行器
      asyncExecutor.shutdown();
    }
    // 执行 SchemaOperationProcessEngineClose 的命令
    commandExecutor.execute(processEngineConfiguration.getSchemaCommandConfig(), new SchemaOperationProcessEngineClose());
    // 如果流程引擎配置类配置了流程引擎的生命周期监听器，则触发流程引擎生命周期的监听器中的onProcessEngineClosed方法。
    if (processEngineConfiguration.getProcessEngineLifecycleListener() != null) {
      processEngineConfiguration.getProcessEngineLifecycleListener().onProcessEngineClosed(this);
    }
    // 转发 ENGINE_CLOSED 事件。
    processEngineConfiguration.getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createGlobalEvent(ActivitiEventType.ENGINE_CLOSED));
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public HistoryService getHistoryService() {
    return historicDataService;
  }

  public RuntimeService getRuntimeService() {
    return runtimeService;
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public DynamicBpmnService getDynamicBpmnService() {
    return dynamicBpmnService;
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

}
