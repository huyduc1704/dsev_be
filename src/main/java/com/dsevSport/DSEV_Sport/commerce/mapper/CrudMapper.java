package com.dsevSport.DSEV_Sport.commerce.mapper;

import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface CrudMapper<E, Res, CreateReq, UpdateReq> {
    Res toResponse(E entity);
    default List<Res> toResponseList(Collection<E> list) {
        return list == null ? List.of() : list.stream().map(this::toResponse).toList();
    }

    //dung de phan trang
    default Page<Res> toResponsePage(Page<E> page) {
        return page.map(this::toResponse);
    }

    //CreateRequest -> Entity
    E toEntity(CreateReq req);

    //only set when field != null
    void updateEntity(UpdateReq req, E entity);

    //entity -> request
    default CreateReq toCreateRequest(E entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default UpdateReq toUpdateRequest(E entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    //update manually
    default <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
