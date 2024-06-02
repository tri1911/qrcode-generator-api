package qrcodeapi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/qrcode")
    public ResponseEntity<?> qrcode(@RequestParam String contents,
                                    @RequestParam(defaultValue = "L") String correction,
                                    @RequestParam(defaultValue = "250") Integer size,
                                    @RequestParam(defaultValue = "png") String type) {
        if (contents == null || contents.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Contents cannot be null or blank"));
        }

        if (!isValidSize(size)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Image size must be between 150 and 350 pixels"));
        }

        if (!isValidCorrectionLevel(correction)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Permitted error correction levels are L, M, Q, H"));
        }

        if (!isValidType(type)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Only png, jpeg and gif image types are supported"));
        }

        BufferedImage image = generateQRCode(contents, size, size, correction);

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("image/" + type))
                .body(image);
    }

    private static boolean isValidSize(int size) {
        return size >= 150 && size <= 350;
    }

    private static boolean isValidCorrectionLevel(String correction) {
        return correction.equals("L") || correction.equals("M") || correction.equals("Q") || correction.equals("H");
    }

    private static boolean isValidType(String mediaType) {
        return mediaType.equals("png") || mediaType.equals("jpeg") || mediaType.equals("gif");
    }

    private static BufferedImage generateQRCode(String data, int width, int height, String correction) {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.valueOf(correction));
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
            return null;
        }
    }
}
