package com.example.SpringBoot.controllers;

import com.example.SpringBoot.dtos.ProductRecordDto;
import com.example.SpringBoot.models.ProductModel;
import com.example.SpringBoot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

///Um DTO (Data Transfer Object) é usado para transferir dados entre o cliente e o servidor
///A classe ResponseEntity em Java é uma abstração que permite manipular a resposta HTTP de um endpoint, incluindo o cabeçalho, o código de estado e o corpo
///A classe Optional do Java é uma forma de lidar com valores que podem ser nulos de forma mais segura e explícita

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository; //ponto de injeção para acesso aos metodos

    //adicionar o produto
    @PostMapping("/products") //metodo de adicionar itens
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel); //conversao de Dto para Model
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    //listar todos os produtos
    @GetMapping("/products") //metodo de leitura
    public ResponseEntity<List<ProductModel>> getAllProducts(){//no corpo possuira uma lista de produtos
        //inserindo hipermidias
        List<ProductModel> productsList = productRepository.findAll();
        if (!productsList.isEmpty()){
            /* Para cada produto primeiro se obtem o id do produto e em seguida adiciona
            o link que direciona a aquele produto em especifico */
            for (ProductModel product : productsList){
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productsList); //status OK (200)
    }

    //listar somente um produto especifico pelo id
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);
        if (productO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found.");
        }
        //hipermidia para mostrar enquanto lista o produto o link referenciando a lista com todos os produtos
        productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List"));

        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }

    //atualizar o produto procurando ele pelo id
    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct (@PathVariable(value= "id") UUID id,
                                                 @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> productO = productRepository.findById(id); //procura o id do produto no banco de dados
        if (productO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found.");
        }
        var productModel = productO.get(); //traz o produto que precisa ser atualizado
        //copia as propriedades de um parametro para outro, copyProperties(param1, param2)
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    //Deletar um produto
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct (@PathVariable(value = "id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);
        if (productO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product Not Found.");
        }
        productRepository.delete(productO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}
