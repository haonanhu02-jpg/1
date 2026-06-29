package cn.iyque.controller;

import cn.iyque.domain.IYquePhaseMockRequest;
import cn.iyque.domain.ResponseResult;
import cn.iyque.service.IYquePhaseOneService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/phase-one/mock", "/phaseOne/mock"})
public class IYquePhaseMockController {

    private final IYquePhaseOneService phaseOneService;

    public IYquePhaseMockController(IYquePhaseOneService phaseOneService) {
        this.phaseOneService = phaseOneService;
    }

    @PostMapping("/scan")
    public ResponseResult mockScan(@RequestBody(required = false) IYquePhaseMockRequest request) {
        return new ResponseResult<>(phaseOneService.mockScan(request));
    }

    @PostMapping("/add-contact")
    public ResponseResult mockAddContact(@RequestBody(required = false) IYquePhaseMockRequest request) {
        return new ResponseResult<>(phaseOneService.mockAddContact(request));
    }

    @PostMapping("/join-group")
    public ResponseResult mockJoinGroup(@RequestBody(required = false) IYquePhaseMockRequest request) {
        return new ResponseResult<>(phaseOneService.mockJoinGroup(request));
    }

    @PostMapping("/leave-group")
    public ResponseResult mockLeaveGroup(@RequestBody(required = false) IYquePhaseMockRequest request) {
        return new ResponseResult<>(phaseOneService.mockLeaveGroup(request));
    }
}
