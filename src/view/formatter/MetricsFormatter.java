package view.formatter;

import model.entity.SystemMetrics;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * VIEW: Formats system metrics for display
 */
public class MetricsFormatter {
    // Fix: Use ZonedDateTime formatter with explicit zone
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public String formatSystemStatus(SystemMetrics metrics, int queueSize, int activeThreads) {
        StringBuilder sb = new StringBuilder();
        // Fix: Format Instant directly with zoned formatter
        sb.append("═══ SYSTEM STATUS [").append(TIMESTAMP_FORMAT.format(Instant.now())).append("] ═══\n");
        sb.append("📊 Current State:\n");
        sb.append("   • Queue Size: ").append(queueSize).append(" tasks\n");
        sb.append("   • Active Workers: ").append(activeThreads).append("\n");
        sb.append("   • Tasks In Progress: ").append(metrics.getTasksInProgress()).append("\n");
        sb.append("\n📈 Throughput:\n");
        sb.append("   • Tasks Submitted: ").append(metrics.getTasksSubmitted()).append("\n");
        sb.append("   • Tasks Completed: ").append(metrics.getTasksCompleted()).append("\n");
        sb.append("   • Tasks Failed: ").append(metrics.getTasksFailed()).append("\n");
        sb.append("   • Completion Rate: ").append(String.format("%.1f%%", metrics.getCompletionRate() * 100)).append("\n");
        sb.append("═══════════════════════════════════════════════════════════");
        return sb.toString();
    }

    public String formatPerformanceMetrics(SystemMetrics metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚡ PERFORMANCE METRICS\n");
        sb.append("   • Average Processing Time: ").append(String.format("%.1fms", metrics.getAverageProcessingTime())).append("\n");
        sb.append("   • Total Processing Time: ").append(metrics.getTotalProcessingTime()).append("ms\n");
        sb.append("   • System Uptime: ").append(formatUptime(metrics.getUptimeSeconds())).append("\n");

        // Calculate throughput
        long uptime = metrics.getUptimeSeconds();
        if (uptime > 0) {
            double tasksPerSecond = (double) metrics.getTasksCompleted() / uptime;
            sb.append("   • Throughput: ").append(String.format("%.2f tasks/second", tasksPerSecond)).append("\n");
        }

        return sb.toString();
    }

    public String formatDetailedReport(SystemMetrics metrics, int queueSize, int activeThreads) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatSystemStatus(metrics, queueSize, activeThreads)).append("\n\n");
        sb.append(formatPerformanceMetrics(metrics));
        return sb.toString();
    }

    private String formatUptime(long uptimeSeconds) {
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}