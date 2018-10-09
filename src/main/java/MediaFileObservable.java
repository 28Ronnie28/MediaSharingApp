import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MediaFileObservable {

    private MediaFile mediaFile;
    private DoubleProperty progress;
    private IntegerProperty type;

    public MediaFileObservable(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
        progress = new SimpleDoubleProperty();
        type = new SimpleIntegerProperty();
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public DoubleProperty progressProperty() {
        progress.set(0);
        if (mediaFile.getFileDownloader() != null) {
            progress.set(((ClientConnectionHandler.FileDownloader) mediaFile.getFileDownloader()).progress.get());
        }
        return progress;
    }

    public IntegerProperty typeProperty() {
        type.set(mediaFile.getValue());
        return type;
    }
}
