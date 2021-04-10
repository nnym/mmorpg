#ifndef MMORPG_WORLD_H
#define MMORPG_WORLD_H

#include <vector>


class World {
public:
    World();

    template <typename T> T spawn(const T* entity);

protected:
    const std::vector<Entity*> enities;
};


#endif //MMORPG_WORLD_H
