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

package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("ticket")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class TicketConfig {
    public TicketConfig() { }

    public TicketConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    private String admId;
    private String userId;
    private String userNick = null;
    private int ocena = -1;
    private String temat;
    private boolean problemRozwiazany = false;
    private String uwaga;

    public boolean isEdited() {
        return getOcena() != 1;
    }

    public boolean exist() {
        return getAdmId() != null && getUserId() != null;
    }

    public String getUrl() {
        return "https://discord.p2w.pl/ticket/" + id;
    }

}
