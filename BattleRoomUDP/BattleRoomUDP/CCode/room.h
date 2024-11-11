#ifndef ROOM_H
#define ROOM_H

#include "entity.h"

/**
 * There should only be one of these running as a server and players should connect to it as clients.
 * author cdgira
 *
 */
struct Room
{
	struct Entity **creatures;
	int num_creatures;
	int max_creatures;
	struct Entity **players;
	int num_players;
	int max_players;
	char **messages;
	int num_messages;
	int max_messages;
	double spawn_chance;
	/**
	 * Kept in milliseconds.
	 */
	int check_spawn;
	
	int creatureID;
};

void initialize_room(struct Room *room, double sc, int cs);
void add_creature(struct Room *room,struct Entity *creature);
void add_message(struct Room *room,char *msg);
void add_player(struct Room *room, struct Entity *player);
void attack_random_entity(struct Room *room,struct Entity *attacker,struct Entity **options,int *num_options);
void print_messages(struct Room *room);
void process_actions(struct Room *room, struct Entity **entities,int num_entities, struct Entity **targets, int *num_targets);
void run(struct Room *room);
void update_creature_action(struct Entity **creatures,int num_creatures);

#endif