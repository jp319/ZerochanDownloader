package jp319.zerochan.utils.statics;

public class OptimalProcessorUtil {
    public static int getOptimalProcessorCount() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Math.max(availableProcessors, 1);
    }
}
