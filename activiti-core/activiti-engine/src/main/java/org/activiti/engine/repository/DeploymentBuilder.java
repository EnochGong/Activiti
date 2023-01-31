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

package org.activiti.engine.repository;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.core.common.project.model.ProjectManifest;
import org.activiti.engine.api.internal.Internal;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipInputStream;

/**
 * Builder for creating new deployments.
 *
 * A builder instance can be obtained through {@link org.activiti.engine.RepositoryService#createDeployment()}.
 *
 * Multiple resources can be added to one deployment before calling the {@link #deploy()} operation.
 *
 * After deploying, no more changes can be made to the returned deployment and the builder instance can be disposed.
 *
 */
@Internal
public interface DeploymentBuilder {
    // 通过 inputStream流的方式部署资源文件 ，流程文档的数据流，对应 ACT_GE_BYTEARRAY 表中BYTES_列。
    // resourceName:资源名称，对应 ACT_GE_BYTEARRAY 表中NAME_列
  DeploymentBuilder addInputStream(String resourceName, InputStream inputStream);

  DeploymentBuilder addInputStream(String resourceName,
                                   Resource resource);
    // 通过资源文件所在的 classpath 进行部署
  DeploymentBuilder addClasspathResource(String resource);
    // 通过字符串的方式 进行部署
  DeploymentBuilder addString(String resourceName, String text);

  DeploymentBuilder addBytes(String resourceName, byte[] bytes);
    // 通过 ZipInputStream 流部署
  DeploymentBuilder addZipInputStream(ZipInputStream zipInputStream);
    // 通过 BpmnModel的方式进行部署
  DeploymentBuilder addBpmnModel(String resourceName, BpmnModel bpmnModel);

  DeploymentBuilder setProjectManifest(ProjectManifest projectManifest);

  DeploymentBuilder setEnforcedAppVersion(Integer enforcedAppVersion);

  /**
   * If called, no XML schema validation against the BPMN 2.0 XSD.
   *
   * Not recommended in general.
   */
  DeploymentBuilder disableSchemaValidation();

  /**
   * If called, no validation that the process definition is executable on the engine will be done against the process definition.
   *
   * Not recommended in general.
   */
  DeploymentBuilder disableBpmnValidation();

  /**
   * Gives the deployment the given name.
   */
  DeploymentBuilder name(String name);

  /**
   * Gives the deployment the given category.
   */
  DeploymentBuilder category(String category);

  /**
   * Gives the deployment the given key.
   */
  DeploymentBuilder key(String key);

  /**
   * Gives the deployment the given tenant id.
   */
  DeploymentBuilder tenantId(String tenantId);

  /**
   * If set, this deployment will be compared to any previous deployment. This means that every (non-generated) resource will be compared with the provided resources of this deployment.
   */
  DeploymentBuilder enableDuplicateFiltering();

  /**
   * Sets the date on which the process definitions contained in this deployment will be activated. This means that all process definitions will be deployed as usual, but they will be suspended from
   * the start until the given activation date.
   */
  DeploymentBuilder activateProcessDefinitionsOn(Date date);

  /**
   * Allows to add a property to this {@link DeploymentBuilder} that influences the deployment.
   */
  DeploymentBuilder deploymentProperty(String propertyKey, Object propertyValue);

  /**
   * Deploys all provided sources to the Activiti engine. 根据提供的部署方式进行资源部署
   */
  Deployment deploy();

}
