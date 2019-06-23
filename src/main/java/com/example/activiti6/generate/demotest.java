package com.example.activiti6.generate;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @ClassName demotest
 * @Description TODO
 * @Author cheng
 * @Date 2019/6/23 13:53
 **/
public class demotest {

    private static final Logger logger = LoggerFactory.getLogger(demotest.class);

    public static void main(String[] args) {
        logger.info("流程启动");
        //创建流程引擎
        ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        logger.info("流程器名称：{};流程器版本号：{}",processEngine.getName(),ProcessEngine.VERSION);
        //部署流程定义文件
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("processes/MyProcess.bpmn").deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        logger.info("流程定义文件名称：{};流程定义文件id：{}",processDefinition.getName(),processDefinition.getId());
        //启动运行流程
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        logger.info("流程实例的定义key：{}",processInstance.getProcessDefinitionKey());
        //处理流程任务
        Scanner scanner = new Scanner(System.in);
        while(processInstance != null && !processInstance.isEnded()){
            TaskService taskService = processEngine.getTaskService();
            List<Task> list = taskService.createTaskQuery().list();
            for (Task task : list) {
                logger.info("待处理任务：{}",task.getName());
                List<FormProperty> formProperties = processEngine.getFormService().getTaskFormData(task.getId()).getFormProperties();
                Map<String,Object> variables = new HashMap<>();
                for (FormProperty formProperty : formProperties) {
                    String line = null;
                    if(StringFormType.class.isInstance(formProperty.getType())){
                        logger.info("请输入：{}",formProperty.getName());
                        line = scanner.nextLine();
                        variables.put(formProperty.getId(),line);
                    }else{
                        logger.info("您输入的类型不支持：{}",formProperty.getType());
                    }
                    logger.info("您输入的内容是：{}",line);
                }
                taskService.complete(task.getId(),variables);
                //查询当前流程实例
                processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
            }
            logger.info("待处理任务数量：{}",list.size());
        }
    }
}
