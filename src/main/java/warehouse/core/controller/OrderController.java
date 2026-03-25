package warehouse.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.core.dto.OrderDTO;
import warehouse.core.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders() {
        return new ResponseEntity<>(orderService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable String id) {
        ResponseEntity<OrderDTO> responseEntity;
        OrderDTO orderDTO = orderService.findById(id);
        if (orderDTO == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(orderDTO, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping
    public void postOrder(@RequestBody OrderDTO orderDTO) {
        orderService.save(orderDTO);
    }

    @PostMapping("/pick/{id}")
    public ResponseEntity<OrderDTO> postPickOrder(@PathVariable String id) {
        ResponseEntity<OrderDTO> responseEntity;
        OrderDTO orderDTO =  orderService.startPicking(id);
        if (orderDTO == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(orderDTO, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping("/ship/{id}")
    public ResponseEntity<OrderDTO> postShipOrder(@PathVariable String id) {
        ResponseEntity<OrderDTO> responseEntity;
        OrderDTO orderDTO =  orderService.ship(id);
        if (orderDTO == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(orderDTO, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<OrderDTO> postCancelOrder(@PathVariable String id) {
        ResponseEntity<OrderDTO> responseEntity;
        OrderDTO orderDTO =  orderService.cancel(id);
        if (orderDTO == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(orderDTO, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<OrderDTO> postReturnOrder(@PathVariable String id) {
        ResponseEntity<OrderDTO> responseEntity;
        OrderDTO orderDTO =  orderService.returnOrder(id);
        if (orderDTO == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(orderDTO, HttpStatus.OK);
        }
        return responseEntity;

    }

}
