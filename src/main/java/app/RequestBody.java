package app;

import java.io.IOException;
import java.io.InputStream;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

final class RequestBody {
    private static final int MAX_JSON_BYTES = 16 * 1024;

    private RequestBody() {
    }

    static JsonNode readJson(InputStream input, JsonMapper mapper) throws IOException {
        byte[] body = read(input);
        try {
            return mapper.readTree(body);
        } catch (StreamReadException | StreamConstraintsException | MismatchedInputException e) {
            throw new BadRequestException("Invalid JSON", e);
        }
    }

    private static byte[] read(InputStream input) throws IOException {
        byte[] body = input.readNBytes(MAX_JSON_BYTES + 1);
        if (body.length > MAX_JSON_BYTES) {
            throw new TooLargeException();
        }
        return body;
    }

    static final class TooLargeException extends IOException {
        TooLargeException() {
            super("JSON body exceeds " + MAX_JSON_BYTES + " bytes");
        }
    }
}
