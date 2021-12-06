package example.micronaut;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;

@Introspected
public class Book {

    @NonNull
    @NotBlank
    private String name;

    public Book() {
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
