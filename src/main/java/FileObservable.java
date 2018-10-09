import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FileObservable {

    private MediaFile classFile;
    private DoubleProperty progress;
    private IntegerProperty type;

    public FileObservable(MediaFile classFile) {
        this.classFile = classFile;
        progress = new SimpleDoubleProperty();
        type = new SimpleIntegerProperty();
    }

    public MediaFile getClassFile() {
        return classFile;
    }

    public DoubleProperty progressProperty() {
        progress.set(0);
        if (classFile.getFileDownloader() != null) {
            progress.set(((ClientConnectionHandler.FileDownloader) classFile.getFileDownloader()).progress.get());
        }
        return progress;
    }

    public IntegerProperty typeProperty() {
        type.set(classFile.getValue());
        return type;
    }
}
