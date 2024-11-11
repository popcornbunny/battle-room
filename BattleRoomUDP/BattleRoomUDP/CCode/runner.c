#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "room.h"

	int main(char **args_c,int args_v)
	{
		struct Room *room = malloc(sizeof(struct Room));
		initialize_room(room,1.0,2000);
		
		struct Entity *player = malloc(sizeof(struct Entity));
		char *p_name = malloc(8*sizeof(char));
		p_name[0] = '\0';
		strcpy(p_name,"Player1");
		initialize_entity(player,p_name,100,100,10,0.6,1000,5000);
		set_action(player,1);
		add_player(room,player);

		player = malloc(sizeof(struct Entity));
		p_name = malloc(8*sizeof(char));
		p_name[0] = '\0';
		strcpy(p_name,"Player2");
		initialize_entity(player,p_name,100,100,10,0.6,1000,5000);
		set_action(player,i%2);
		add_player(room,player);
		
		run(room);
		

	}

