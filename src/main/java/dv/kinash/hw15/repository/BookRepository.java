package dv.kinash.hw15.repository;

import dv.kinash.hw15.repository.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import dv.kinash.hw15.repository.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findByReader(User reader);

    @Query("Select b, b.reader is null as hasReader From Book b")
    List<Object> findAllWithReader();
}
