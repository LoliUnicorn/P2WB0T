/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.logs;

import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.redis.RedisManager;
import pl.kamil0024.logs.logger.Logger;
import pl.kamil0024.logs.logger.MessageManager;
import pl.kamil0024.stats.StatsModule;

import javax.inject.Inject;

public class LogsModule implements Modul {
    
    @Inject ShardManager api;

    private boolean start = false;

    private MessageManager messageManager;
    private Logger logger;
    private final StatsModule statsModule;
    private final RedisManager redisManager;

    public LogsModule(ShardManager api, StatsModule statsModule, RedisManager redisManager) {
        this.api = api;
        this.statsModule = statsModule;
        this.redisManager = redisManager;
    }
    
    @Override
    public boolean startUp() {
        messageManager = new MessageManager(redisManager);
        logger = new Logger(messageManager, api, statsModule);
        api.addEventListener(messageManager, logger);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(messageManager, logger);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public boolean isStart() {
        return this.start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

}
