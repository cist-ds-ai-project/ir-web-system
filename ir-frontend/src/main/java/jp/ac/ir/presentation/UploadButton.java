package jp.ac.ir.presentation;

import com.vaadin.flow.component.upload.UploadI18N;

import java.util.Arrays;

public class UploadButton extends UploadI18N {
    public UploadButton() {
        setDropFiles(new DropFiles().setOne("ここにファイルをドロップしてください")
                .setMany("ここにファイルをドロップしてください"));
        setAddFiles(new AddFiles().setOne("ファイルのアップロード")
                .setMany("ファイルのアップロード"));
        setError(new Error().setTooManyFiles("ファイルが多すぎます")
                .setFileIsTooBig("ファイルが多すぎます")
                .setIncorrectFileType("ファイルの形式が異なります"));
        setUploading(new Uploading()
                .setStatus(new Uploading.Status().setConnecting("Connecting...")
                        .setStalled("Stalled")
                        .setProcessing("Processing File...").setHeld("Queued"))
                .setRemainingTime(new Uploading.RemainingTime()
                        .setPrefix("残り時間： ")
                        .setUnknown("時間計測中"))
                .setError(new Uploading.Error()
                        .setServerUnavailable(
                                "アップロードに失敗しました、時間をおいてもう一度ためしてください")
                        .setUnexpectedServerError(
                                "サーバーエラーです")
                        .setForbidden("Upload forbidden")));
        setUnits(new Units().setSize(Arrays.asList("B", "kB", "MB", "GB", "TB",
                "PB", "EB", "ZB", "YB")));
    }
}
