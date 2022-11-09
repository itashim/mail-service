package kz.bsbnb.microservices.nbcloud.mail.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kz.bsbnb.microservices.nbcloud.mail.entity.ErrorEntity;
import kz.bsbnb.microservices.nbcloud.mail.service.ErrorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/log")
@Api(tags = {"Log controller"}, description = "Контроллер предназначен для логирования ошибок и работы с ними")
public class LogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogController.class);

    @Autowired
    private ErrorService errorService;

    /**
     * READ
     */

    @GetMapping("/errors")
    @ApiOperation("Возвращает список всех сущностей, которым не вышло доставить сообщение")
    public @ResponseBody
    List<ErrorEntity> getErrors() {
        List<ErrorEntity> errors = errorService.findAll();
        LOGGER.info(":: LogController :: /log/errors - найдено {} записей", errors.size());
        return errors;
    }

    @GetMapping("/errors/{email}")
    @ApiOperation("Возвращает список сущностей по емейлу, которым не вышло доставить сообщение")
    public @ResponseBody
    List<ErrorEntity> getErrorsByEmail(@PathVariable String email) {
        List<ErrorEntity> errors = errorService.getByEmail(email);
        LOGGER.info(":: LogController :: /log/errors/{} - найдено {} записей", email, errors.size());
        return errors;
    }

    @GetMapping("/error")
    @ApiOperation("Возвращает сущность по айдии, которой не вышло доставить сообщение")
    public @ResponseBody
    ErrorEntity getErrorById(@RequestParam(value = "id") long id) {
        ErrorEntity errorEntity = errorService.getById(id);
        LOGGER.info(":: LogController :: /log/error?id={} - найдена запись", id);
        return errorEntity;
    }

    /**
     * WRITE
     */

    @PostMapping("/insert")
    @ApiOperation("Запись в БД сущности, которой не вышло доставить сообщение")
    public @ResponseBody
    ResponseEntity<Object> logError(@RequestBody ErrorEntity errorDto) {
        LOGGER.info(":: LogController :: /log/insert - сохранение записи об ошибке с почтой {}", errorDto.getEmail());
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setError(errorDto.getError());
        errorEntity.setBody(errorDto.getBody());
        errorEntity.setDate(errorDto.getDate());
        errorEntity.setEmail(errorDto.getEmail());
        errorEntity.setSubject(errorDto.getSubject());
        errorEntity.setSystem(errorDto.getSystem());
        LOGGER.info(errorEntity.toString());
        return ResponseEntity
                .ok(errorService.save(errorEntity));
    }

    /**
     * DELETE
     */

    @PostMapping("/delete")
    @ApiOperation("Удаление сущности из БД")
    public boolean deleteError(@RequestParam(value = "id") long id) {
        LOGGER.info(":: LogController :: /log/delete?id={} удаление записи с айди {}", id, id);
        try {
            errorService.deleteById(id);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
