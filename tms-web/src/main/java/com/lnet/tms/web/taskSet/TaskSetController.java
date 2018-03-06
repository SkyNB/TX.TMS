package com.lnet.tms.web.taskSet;

import com.lnet.framework.core.PageResponse;
import com.lnet.framework.core.Response;
/*import com.lnet.microservices.dispatch.contract.TaskTeam;
import com.lnet.tms.application.TaskSet.TaskSetApplication;
import com.lnet.tms.application.common.KendoGridRequest;*/
import com.lnet.tms.web.util.UserPrincipalImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2016/12/12.
 */
@RequestMapping("/taskset")
@Controller
public class TaskSetController {
/*    @Autowired
    private TaskSetApplication taskSetApplication;*/
    @Autowired
    private UserPrincipalImpl userPrincipal;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap map) {
//        map.addAttribute("taskTeamTypes", TaskTeam.TaskTeamType.values());
        return "taskset/index";
    }

//    @RequestMapping(value = "/addtask", method = RequestMethod.GET)
//    public String addTaskSet(ModelMap map) {
//        map.addAttribute("taskTeamTypes", TaskTeam.TaskTeamType.values());
//        return "taskset/addtask";
//    }

    @RequestMapping(value = "/importtask", method = RequestMethod.GET)
    public String importTask() {
        return "taskset/importtask";
    }

    @RequestMapping(value = "/clientserver", method = RequestMethod.GET)
    public String clientTask() {
        return "taskset/clientserver";
    }

//    @RequestMapping(value = "/search", method = RequestMethod.POST)
//    @ResponseBody
//    public PageResponse<TaskTeam> search(@RequestBody KendoGridRequest params) {
//        params.setParams("branchCode", userPrincipal.getCurrentBranchCode());
//        params.setParams("siteCode", userPrincipal.getCurrentSiteCode());
//        return taskSetApplication.search(params);
//    }

//    @RequestMapping(value = "/create", method = RequestMethod.POST)
//    @ResponseBody
//    public Response create(@RequestBody TaskTeam taskTeam) {
//        taskTeam.setBranchCode(userPrincipal.getCurrentBranchCode());
//        taskTeam.setSiteCode(taskTeam.getSiteCode());
//        return taskSetApplication.create(taskTeam);
//    }

//    @RequestMapping(value = "/edit", method = RequestMethod.POST)
//    @ResponseBody
//    public Response update(@RequestBody TaskTeam taskTeam) {
//        return taskSetApplication.update(taskTeam);
//    }

//    @RequestMapping(value = "/get/{taskTeamId}", method = RequestMethod.GET)
//    @ResponseBody
//    public Response get(@PathVariable String taskTeamId) {
//        return taskSetApplication.get(taskTeamId);
//    }
}
