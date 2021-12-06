package example.micronaut;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class BookSaved {

    @NonNull
    @NotBlank
    private String name;

    @NonNull
    @NotBlank
    private String isbn;

    public BookSaved() {

    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(@NonNull String isbn) {
        this.isbn = isbn;
    }
}
