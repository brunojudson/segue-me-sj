package br.com.segueme.service;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Serviço para geração de QR Codes usando ZXing.
 */
@ApplicationScoped
public class QrCodeService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int QR_WIDTH = 250;
    private static final int QR_HEIGHT = 250;

    /**
     * Gera um QR Code como imagem PNG codificada em Base64.
     *
     * @param conteudo Texto/URL a ser codificado no QR Code
     * @return String Base64 da imagem PNG do QR Code
     */
    public String gerarQrCodeBase64(String conteudo) {
        if (conteudo == null || conteudo.trim().isEmpty()) {
            return null;
        }
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(conteudo, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Erro ao gerar QR Code", e);
        }
    }

    /**
     * Gera a tag HTML completa para exibir o QR Code como imagem inline.
     *
     * @param conteudo Texto/URL a ser codificado
     * @return String com data URI da imagem (para uso em src de img)
     */
    public String gerarQrCodeDataUri(String conteudo) {
        String base64 = gerarQrCodeBase64(conteudo);
        if (base64 == null) {
            return null;
        }
        return "data:image/png;base64," + base64;
    }
}
