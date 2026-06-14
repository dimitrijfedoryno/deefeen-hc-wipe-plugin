package cz.deefeen.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class HcScoreboard {

    private final Scoreboard board;
    private final Objective objective;
    private final Score score;
    private int currentCount;

    public HcScoreboard(String title, String line, int initialCount) {
        this.currentCount = initialCount;

        board = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("hcWipeCount", Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        score = objective.getScore(line);
        score.setScore(currentCount);
    }

    public void setCount(int count) {
        this.currentCount = count;
        score.setScore(count);
    }

    public void increment() {
        setCount(currentCount + 1);
    }

    public void reset() {
        setCount(0);
    }

    public void applyToPlayer(Player player) {
        player.setScoreboard(board);
    }

    public void applyToAll() {
        Bukkit.getOnlinePlayers().forEach(this::applyToPlayer);
    }
}
