package net.samagames.api.games;

import net.samagames.api.SamaGamesAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class BeginTimer implements Runnable
{
    private static final int timeStart = 30;
    private final Game game;
    private final SamaGamesAPI api;
    private int time;
    private boolean ready;

    public BeginTimer(Game game)
    {
        this.game = game;
        this.api = SamaGamesAPI.get();
        this.time = timeStart;
        this.ready = false;
    }
 
    @Override
    public void run()
    {
        int nPlayers = this.game.getConnectedPlayers();
 
        if (nPlayers >= api.getGameManager().getGameProperties().getMinSlots() && !this.ready)
        {
            this.ready = true;
            this.game.setStatus(Status.READY_TO_START);
            this.time = timeStart;
        }

        if (nPlayers < api.getGameManager().getGameProperties().getMinSlots() && this.ready)
        {
            this.ready = false;
            this.game.setStatus(Status.WAITING_FOR_PLAYERS);

            api.getGameManager().getCoherenceMachine().getMessageManager().writeNotEnougthPlayersToStart();
            
            for (Player p : Bukkit.getOnlinePlayers())
                p.setLevel(timeStart);
        }

        if (this.ready)
        {
            this.time--;
            double pourcentPlayer = (game.getConnectedPlayers()/api.getGameManager().getGameProperties().getMaxSlots());
            if(time > 5 && pourcentPlayer >= 0.98)
            {
                time = 5;
            }

            api.getGameManager().getCoherenceMachine().getMessageManager().writeGameStartIn(this.time);
            this.sendSound(this.time);
            
            if(this.time <= 0)
            {
                Bukkit.getScheduler().runTask(api.getPlugin(), () -> {
                    try{
                        game.startGame();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });

                game.getBeginTimer().cancel();
            }
        }
    }

    public void sendSound(int seconds)
    {
        boolean ring = false;
        
        if (seconds <= 5 && seconds != 0)
        {
            ring = true;
        }
        
        for(Player player : Bukkit.getOnlinePlayers())
        {
            player.setLevel(seconds);
            
            if (ring)
                player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);

            if (seconds == 0)
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
        }
    }
}