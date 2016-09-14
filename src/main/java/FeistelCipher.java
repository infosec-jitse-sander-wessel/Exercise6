import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Created by Sander on 14-9-2016.
 */
public class FeistelCipher {

    private byte[] roundKeys;
    private boolean decrypt = false;

    public FeistelCipher(String passPhrase) {
        roundKeys = createRounds(passPhrase);
    }

    private byte[] createRounds(String passPhrase) {
        String firstShaString = DigestUtils.sha256Hex(passPhrase);
        byte[] firstShaBytes = DigestUtils.sha256(passPhrase);
        byte[] secondShaBytes = DigestUtils.sha256(firstShaString);

        return ArrayUtils.addAll(firstShaBytes, secondShaBytes);
    }

    public byte[] run(byte[] input) {
        return mapLine(input);
    }

    private byte[] mapLine(byte[] input) {
        byte[] bytes = ArrayUtils.addAll(input, getPadding(input.length));

        byte[] result = new byte[0];

        for (int blockStart = 0; blockStart < bytes.length; blockStart += 8) {
            byte[] blockToEncrypt = Arrays.copyOfRange(bytes, blockStart, blockStart + 8);
            for (int rounds = 0; rounds < 16; rounds++) {
                byte[] newLeft = Arrays.copyOfRange(blockToEncrypt, 4, 8);
                byte[] newRight = Arrays.copyOfRange(blockToEncrypt, 0, 4);
                if (decrypt) {
                    newLeft = getRightHalf(newLeft, rounds * 4);
                } else {
                    newRight = getRightHalf(newRight, rounds * 4);
                }
                blockToEncrypt = ArrayUtils.addAll(newLeft, newRight);
            }
            result = ArrayUtils.addAll(result, blockToEncrypt);
        }
        return result;
    }

    private byte[] getPadding(int length) {
        int padding = 8 - (length % 8);
        byte[] padded = new byte[padding];
        Arrays.fill(padded, (byte) padding);
        return padded;
    }

    private byte[] getRightHalf(byte[] leftHalf, int roundStart) {
        byte[] result = new byte[leftHalf.length];
        for (int i = 0; i < leftHalf.length; i++) {
            result[i] = (byte) (leftHalf[i] ^ roundKeys[roundStart + i]);
        }
        return result;
    }

    public FeistelCipher setDecrypting(boolean decrypt) {
        this.decrypt = decrypt;
        if (decrypt) {
            byte[] reversedRounds = new byte[0];
            for (int i = roundKeys.length; i > 3; i -= 4) {
                byte[] block = Arrays.copyOfRange(roundKeys, i - 4, i);
                reversedRounds = ArrayUtils.addAll(reversedRounds, block);
            }
            roundKeys = reversedRounds;
        }
        return this;
    }
}
