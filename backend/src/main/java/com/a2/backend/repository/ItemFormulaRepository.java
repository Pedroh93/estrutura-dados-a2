package com.a2.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.a2.backend.entity.ItemFormulaEntity;

public interface ItemFormulaRepository extends JpaRepository<ItemFormulaEntity, Integer> {

    @Query("select i from ItemFormulaEntity i left join fetch i.pai where i.produto.id = :produtoId order by i.id")
    List<ItemFormulaEntity> buscarDoProduto(@Param("produtoId") Integer produtoId);

    Optional<ItemFormulaEntity> findByProdutoIdAndPaiIsNull(Integer produtoId);

    long countByProdutoId(Integer produtoId);
}
