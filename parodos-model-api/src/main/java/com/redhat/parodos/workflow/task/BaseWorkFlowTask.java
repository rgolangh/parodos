/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.BeanNameAware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base Class for a WorkFlowTask.
 * <p>
 * This includes logic for getting Parameter values from the WorkContext. The bean name is
 * used when Reading from the WorkContextDelegate as more than one WorkflowTask might have
 * the same ParameterName
 *
 * @author Luke Shannon (Github: lshannon)
 */
@Slf4j
public abstract class BaseWorkFlowTask implements WorkFlowTask, BeanNameAware {

	@Getter
	private String name;

	@Override
	public void setBeanName(String name) {
		this.name = name;
	}

	public String getParameterValue(WorkContext workContext, String parameterName) throws MissingParameterException {
		return Optional
				.ofNullable(new ObjectMapper().convertValue(
						WorkContextDelegate.read(workContext, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
								name, WorkContextDelegate.Resource.ARGUMENTS),
						new TypeReference<HashMap<String, String>>() {
						}))
				.orElse(new HashMap<>()).entrySet().stream().filter(entry -> parameterName.equals(entry.getKey()))
				.map(Map.Entry::getValue).findFirst().orElseThrow(() -> {
					log.error(String.format("parameter %s is not provided for task %s!", parameterName, name));
					return new MissingParameterException("missing parameter(s)");
				});
	}

}