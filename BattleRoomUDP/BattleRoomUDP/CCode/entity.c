#include <string.h>

#include "entity.h"

 

	void initialize_entity(struct Entity *entity, char *n, int h, int mh, int d, double hc, int ar, int hr)
	{
		clock_t CLOCKS_PER_MILI = CLOCKS_PER_SEC / 1000;
		
		entity->name[0] = '\0';
		strcpy(entity->name,n);
		entity->health = h;
		entity->max_health = mh;
		entity->damage = d;
		entity->hit_chance = hc;
		entity->attack_rate = ar;
		entity->heal_rate = hr;
		entity->start_time = clock()/CLOCKS_PER_MILI;
		entity->last_attack = 0;
		entity->last_heal = 0;
		entity->action = -1;
	}
	
	void heal(struct Entity *entity, long time)
	{
		entity->health += 5;
		if (entity->health > entity->max_health)
			entity->health = entity->max_health;
		entity->last_heal = time;
	}
	
	/**
	 * Updates the value in action, if outside the allowed actions sets it to -1 (do nothing).
	 * param value
	 */
	void set_action(struct Entity *entity, int value)
	{
		entity->action = value;
		if ((entity->action < -1) || (entity->action > 2))
		{
			entity->action = -1;
		}
	}
	
	void take_damage(struct Entity *entity, int dmg)
	{
		if (entity->action != 0)
		    entity->health -= dmg;
		else  // We are defending.
			entity->health -= dmg/2;
		if (entity->health < 0)
			entity->health = 0;
	}
