#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>

#include "room.h"

    clock_t present_time = 0;
	int run_flag = 1;
	 
	void initialize_room(struct Room *room, double sc, int cs)
	{
		room->creatures = malloc(10*sizeof(struct Entity*));
		room->num_creatures = 0;
		room->max_creatures = 10;
		room->players = malloc(10*sizeof(struct Entity*));
		room->num_players = 0;
		room->max_players = 10;
		room->messages = malloc(10*sizeof(char*));
		room->num_messages = 0;
		room->max_messages = 10;
		room ->spawn_chance = sc;
		room->check_spawn = cs;
		room->creatureID = 1;
	}

    void add_creature(struct Room *room,struct Entity *creature)
	{
		room->creatures[room->num_creatures] = creature;
		room->num_creatures++;
		if (room->num_creatures == room->max_creatures)
		{
			room->max_creatures = room->max_creatures*2;
			struct Entity **tmp = malloc(room->max_creatures*sizeof(struct Entity*));
			for (int i=0;i<room->num_creatures;i++)
			{
				tmp[i] = room->creatures[i];
			}
			free(room->creatures);
			room->creatures = tmp;
		}
		char *msg = malloc((strlen(creature->name)+23)*sizeof(char));
		msg[0] = '\0';
		strcpy(msg,creature->name);
		strcat(msg," has entered the room.");
		add_message(room,msg);
		room->creatureID++;
	}

	void add_message(struct Room *room,char *msg)
	{
        room->messages[room->num_messages] = msg;
		room->num_messages++;
		if (room->num_messages == room->max_messages)
		{
			room->max_messages = room->max_messages*2;
			char **tmp = malloc((room->max_messages)*sizeof(char*));
			for (int i=0;i<room->num_messages;i++)
			{
				tmp[i] = room->messages[i];
			}
			free(room->messages);
			room->messages = tmp;
		}
	}
	
	void add_player(struct Room *room, struct Entity *player)
	{
		room->players[room->num_players] = player;
		room->num_players++;
		if (room->num_players == room->max_players)
		{
			room->max_players = room->max_players*2;
			struct Entity **tmp = malloc((room->max_players)*sizeof(struct Entity*));
			for (int i=0;i<room->num_players;i++)
			{
				tmp[i] = room->players[i];
			}
			free(room->players);
			room->players = tmp;
		}
		char *msg = malloc((strlen(player->name)+23)*sizeof(char));
		msg[0] = '\0';
		strcpy(msg,player->name);
		strcat(msg," has entered the room.");
		add_message(room,msg);
	}
	
	void attack_random_entity(struct Room *room, struct Entity *attacker,struct Entity **options,int *num_options)
	{
		double check = rand() / (double) (RAND_MAX);
		int index = (int)(check*(*num_options));
		struct Entity *target = options[index];
		int targetHealth = target->health;
		take_damage(target,attacker->damage);

        char result[12];
		char *msg = malloc((strlen(attacker->name)+strlen(target->name)+strlen(result)+26)*sizeof(char));
		msg[0] = '\0';
		strcpy(msg,attacker->name);
		strcat(msg," attacked ");
		strcat(msg,target->name);
		strcat(msg," doing ");
		strcat(msg,result);
		strcat(msg," damage.");
		add_message(room,msg);

		attacker->last_attack = present_time;
		if (target->health == 0)
		{
			options[index] = NULL;
			for (int i=index;i<*num_options;i++)
			{
			    options[i] = options[i+1];
			}
			msg = malloc((strlen(target->name)+9)*sizeof(char));
			msg[0] = '\0';
		    strcpy(msg,target->name);
		    strcat(msg," killed.");
			add_message(room,msg);
			free(target);
			(*num_options)--;
		}	
	}
	
	void print_messages(struct Room *room)
	{
		for (int i=0;i<room->num_messages;i++)
		{
			printf("%s\n",room->messages[i]);
			free(room->messages[i]);
			room->messages[i] = NULL;
		}
		room->num_messages = 0;
	}
	
	void process_actions(struct Room *room, struct Entity **entities,int num_entities, struct Entity **targets, int *num_targets)
	{
		for (int i=0;i<num_entities;i++)
		{
			int action = entities[i]->action;
			if ((action == 1) && (*num_targets > 0))
			{
				long last_attack = entities[i]->last_attack;
				if ((present_time - last_attack) > entities[i]->attack_rate)
				{
					attack_random_entity(room,entities[i],targets,num_targets);
				}
			}
			if (action == 2)
			{
				long last_heal = entities[i]->last_heal;
				if ((present_time - last_heal) > entities[i]->heal_rate)
				{
					int entity_health = entities[i]->health;
			        heal(entities[i],present_time);
					char *msg = malloc((strlen(entities[i]->name)+23)*sizeof(char));
		            msg[0] = '\0';
					strcpy(msg,entities[i]->name);
					strcat(msg," healed for ");
					int change = entities[i]->health-entity_health;
					char result[10];  // So long as health doesn't go over 9 digits we are good.
					sprintf(result, "%i", change);
                    strcat(msg,result);
					add_message(room,msg);
				}
			}	
		}
	}
	
	void run(struct Room *room)
	{
		clock_t CLOCKS_PER_MILI = CLOCKS_PER_SEC / 1000; // This really should be a global

		clock_t start_time = clock();
		clock_t last_spawn_check = start_time;
		while (run_flag)
		{
			present_time = clock()/CLOCKS_PER_MILI;
			if ((present_time - last_spawn_check) > room->check_spawn)
			{
				double check = rand() / (double) (RAND_MAX);
				if (room->spawn_chance > check)
				{
					struct Entity *creature = malloc(sizeof(struct Entity));
					char result[12];
					sprintf(result,"%i",room->creatureID);
					char *name = malloc((9+strlen(result))*sizeof(char));
					name[0] = '\0';
					strcpy(name,"Creature");
					strcat(name,result);
					initialize_entity(creature,name,100,100,10,0.6,1000,5000);
					add_creature(room,creature);
				}
				last_spawn_check = present_time;
			}
			
			process_actions(room,room->players,room->num_players,room->creatures,&(room->num_creatures));
			process_actions(room,room->creatures,room->num_creatures,room->players,&(room->num_players));
			
			update_creature_action(room->creatures,room->num_creatures);
			print_messages(room);
		}
	}
	
	void update_creature_action(struct Entity **creatures,int num_creatures)
	{
		for (int i=0;i<num_creatures;i++)
		{
			long last_attack = creatures[i]->last_attack;
			if ((present_time - last_attack) > creatures[i]->attack_rate)
				set_action(creatures[i],1); // Can attack so do that.
			else
			{
				long last_heal = creatures[i]->last_heal;
				if ((present_time - last_heal) > creatures[i]->heal_rate)
					set_action(creatures[i],2); // If can't attack, but can heal do that.
				else
					set_action(creatures[i],0); // If can't attack or heal just defend.
			}
		}
	}
