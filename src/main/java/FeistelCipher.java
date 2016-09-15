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

    public byte[] encrypt(byte[] input) {
        byte[] paddedInput = ArrayUtils.addAll(input, getPadding(input.length));
        return encrypt(paddedInput, roundKeys);
    }

    public byte[] decrypt(byte[] input) {
        byte[] reversedRounds = getReversedRounds(roundKeys);
        byte[] reversedBlocksInput = reverseBlocks(input);
        byte[] result = encrypt(reversedBlocksInput, reversedRounds);
        result = reverseBlocks(result);
        int padding = result[result.length - 1];
        result = Arrays.copyOf(result, result.length - padding);
        return result;
    }

    private byte[] encrypt(byte[] input, byte[] rounds) {
        byte[] result = new byte[0];

        // we run over the input one block at a time
        for (int blockStart = 0; blockStart < input.length; blockStart += 8) {
            byte[] blockToEncrypt = Arrays.copyOfRange(input, blockStart, blockStart + 8);

            // we will run 16 rounds for each block
            for (int roundCounter = 0; roundCounter < 16; roundCounter++) {
                // next round left and right will be swapped
                byte[] newLeft = Arrays.copyOfRange(blockToEncrypt, 4, 8);
                byte[] newRight = Arrays.copyOfRange(blockToEncrypt, 0, 4);
                // the right side will be xored with our current rounds key schedule result
                newRight = xor(rounds, newRight, roundCounter * 4);

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

    private byte[] xor(byte[] keys, byte[] leftHalf, int roundStart) {
        byte[] result = new byte[leftHalf.length];
        for (int i = 0; i < leftHalf.length; i++) {
            result[i] = (byte) (leftHalf[i] ^ keys[roundStart + i]);
        }
        return result;
    }

    private byte[] getReversedRounds(byte[] rounds) {
        byte[] reversedRounds = new byte[0];
        for (int i = rounds.length; i > 3; i -= 4) {
            byte[] block = Arrays.copyOfRange(rounds, i - 4, i);
            reversedRounds = ArrayUtils.addAll(reversedRounds, block);
        }
        return reversedRounds;
    }

    private byte[] reverseBlocks(byte[] input) {
        byte[] result = new byte[0];
        for (int blockStart = 0; blockStart < input.length; blockStart += 8) {
            byte[] newLeft = Arrays.copyOfRange(input, blockStart + 4, blockStart + 8);
            byte[] newRight = Arrays.copyOfRange(input, blockStart, blockStart + 4);
            byte[] reversed = ArrayUtils.addAll(newLeft, newRight);
            result = ArrayUtils.addAll(result, reversed);
        }
        return result;
    }
}
