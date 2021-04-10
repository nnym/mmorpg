#include "World.h"


World::World() {
    this->enities = std::vector<Entity>();
}

template<typename T>
T World::spawn(const T* entity) {
    this->enities.push_back(entity);
}
