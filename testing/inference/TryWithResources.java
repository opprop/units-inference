import units.qual.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// @skip-test
// unable to insert inferred annotations for resource variables in try blocks
// TODO: try variable defaulting? implicits?

class TryWithResources {

    String readFirstLineFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }
}
