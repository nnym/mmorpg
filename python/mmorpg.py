#!/usr/bin/env python3
from curses import cbreak, echo, endwin, initscr, nocbreak, noecho, start_color
from curses_io import Pad
from multiprocessing import Process
from os.path import isfile
from random import choice, randint, uniform
from re import search


class World:
    def __init__(self):
        self.advs = ['frustratingly', 'shockingly', 'annoyingly',
                     'nerve-wrackingly']
        self.adjs = ['useless', 'overwhelming', 'underwhelming', 'bad',
                     'egregious', 'indifferent', 'mad']
        self.entities = set()
        self.occupied = set()

    def get_entity(self, name):
        for entity in self.entities:
            if entity.name == name:
                return entity

    def update_occupied(self):
        self.occupied = set()

        for entity in self.entities:
            self.occupied.add(entity.pos)

    def spawn_goblin(self):
        name = f'{choice(self.advs)} {choice(self.adjs)} goblin'
        health = randint(1, 100)
        gold = randint(0, 10)
        damage = randint(1, 7)
        precision = uniform(0.3, 1)
        pos = (user.pos[0], user.pos[1])
        pos = tuple(map(lambda c: c + round(uniform(-10, 10), 2), pos))

        if pos in self.occupied or self.get_entity(name):
            self.spawn_goblin()
            return

        world.entities.add(Goblin(name, health, gold, damage, precision, pos))

    def spawn_goblin_group(self):
        quantity = randint(1, 6)

        for goblin in range(quantity):
            self.spawn_goblin()


class Entity:
    def __init__(self, name, health, gold, damage, precision, pos):
        self.name = name
        self.health = health
        self.gold = gold
        self.damage = damage
        self.precision = precision
        self.type = type(self).__name__.lower()
        self.pos = pos
        world.occupied.add(self.pos)

    def attack(self, target=None):
        if not target:
            if not len(self.others()):
                pad.dynamic('You are alone.', -50)
                return
            else:
                target = self.nearest()

        distance = self.distance(target)

        if distance:
            if sum(map(abs, distance)) > 1:
                pad.dynamic(f'{target.name} is not in range.', -50)
            else:
                if self.precision >= uniform(0, 1):
                    if target.health <= 0:
                        gold = 'did not drop any gold' if target.gold == 0 \
                                else f'dropped {target.gold} gold'
                        pad.dynamic(f'{target.name.capitalize()} {gold}.', -50)

                        if target.gold != 0:
                            selection = pad.ask('Will you take it?', dynamic=-50).lower()
                            yes = selection not in ('n', 'no', 'nope')
                            self.gold += target.gold if yes else 0

                        world.entities.remove(target)
                        return

                    target.health -= self.damage
                    target.health = 0 if target.health < 0 else target.health
                    pad.dynamic(f'You inflicted {self.damage} damage to {target.name}', -50)
                    pad.dynamic(f'Remaining health: {target.health}', -50)
                else:
                    pad.dynamic(f'You missed {target.name}.', -50)
        else:
            pad.dynamic(distance, -50)

    def distance_wrapper(self, target=None):
        if not target:
            target = self.nearest()
        else:
            target = world.get_entity(target)
            
            if not target:
                return f'"{target.name}" does not exist.'

        return f'{target.name} is {self.distance(target)} meters away.'

    def distance(self, target):
        return target.pos[0] - self.pos[0], target.pos[1] - self.pos[1]

    def info(self, target=None):
        if not target:
            target = self
        else:
            target = world.get_entity(target)

            if not target:
                return f'"{target}" does not exist.'

        entries = target.__dict__
        info = ''

        for attr, value in entries.items():
            if attr == 'pos':
                value = f'position: ({value[0]}, {value[1]})\n'
            else:
                value = f'{attr}: {value}\n'

            info += value

        return info

    def move(self, pos):
        if pos not in world.occupied:
            pad.dynamic(f'{(self.pos[0], self.pos[1])} => {(pos[0], pos[1])}', -50)
            world.occupied.remove(self.pos)
            world.occupied.add(pos)
            self.pos = pos
        else:
            pad.dynamic('You cannot go there; that area is occupied.', -50)

    def seek(self):
        if world.entities == set():
            pad.dynamic('You are alone.', -50)
        else:
            entity = self.nearest()
            pos = (entity.pos[0] - .01, entity.pos[1])
            self.move(pos)

    def nearest(self):
        others = self.others()

        if others == {}:
            return 'You are alone.'

        nearest_dist = 0

        for entity in others:
            dist = self.distance(entity)
            dist = sum(dist)

            if dist > nearest_dist:
                nearest_dist = dist
                nearest = entity

        return nearest

    def others(self):
        others = world.entities - {self}
        return others

    def say(self, text):
        pad.dynamic(f'{self.name}: {text}', -50)

    def __del__(self):
        world.occupied.remove(self.pos)


class User(Entity):
    def look(self):
        nearby = ''

        if self.others() == set():
            world.spawn_goblin()

        for entity in self.others():
            nearby += f'position of {entity.name}: {(entity.pos[0], entity.pos[1])}\n'

        pad.dynamic(nearby, -50)


class Goblin(Entity):
    def __init__(self, *args):
        super().__init__(*args)
        self.height = randint(40, 120)


class Exit(Exception):
    pass


def inp():
    while True:
        words = list(map(lambda x: x.lower(), pad.ask().split()))

        try:
            verb = words.pop(0)
        except IndexError:
            return

        if verb == 'help':
            if not words:
                pad.dynamic(guide, -50, erase=False, wait=False)
            else:
                command = words.pop(0)

                if command == 'move':
                    pad.dynamic('This command takes cardinal direction and (optionally) '
                                'distance. Below is an example.\n\tmove north 3.2', -50)

        elif verb == 'attack':
            user.attack(' '.join(words))
        elif verb == 'distance':
            pad.dynamic(user.distance_wrapper(' '.join(words)), -50)
        elif verb in ('information', 'info'):
            pad.dynamic(user.info(' '.join(words)), -50, erase=False, wait=False)
        elif verb == 'look':
            user.look()
        elif verb == 'say':
            user.say(' '.join(words))
        elif verb == 'seek':
            user.seek()
        elif verb == 'move':
            if not words:
                pos = (0, 0)
            else:
                direction = words.pop(0)
                pos = list(user.pos)

                if direction not in ['north', 'east', 'south', 'west']:
                    return
                else:
                    distance = search(r'(\d+.?d*)(.\d+)', words.pop(0))

                    if distance:
                        distance = round(float(distance[0]), 2)
                    else:
                        pad.dynamic('Distance must be a numerical value.', -50)
                        return

                if direction == 'north':
                    pos[1] += distance
                elif direction == 'east':
                    pos[0] += distance
                elif direction == 'south':
                    pos[1] -= distance
                elif direction == 'west':
                    pos[0] -= distance

            pos = tuple(pos)
            user.move(pos)
        elif verb == 'exit':
            raise Exit
        elif verb != '':
            pad.dynamic(f'"{verb}" is not recognized as a valid command.', -50)


try:
    scr = initscr()
    noecho()
    cbreak()
    scr.keypad(1)

    try:
        start_color()
    except:
        pass

    pad = Pad(scr)
    world = World()
    username = pad.ask('Enter your username.', dynamic=-50)
    user = User(username, 30, 0, 1, 0.85, (0, 0))
    world.entities.add(user)
    fname = f'goblin.{user.name}'

    if isfile(fname):
        with open(fname) as f:
            for line in f.readlines():
                line = line.split(':')
                attr, value = line[0], line[1].strip()
                user.attr = value

    world.spawn_goblin_group()

    guide = ('\nhelp: display this message.\n'
             'attack: try to attack the entity whose name follows this command.\n'
             'distance: output the distance between yourself and a target.\n'
             'information: view information about yourself or another entity.\n'
             'look: search for nearby entities.\n'
             'move: move your character in a cardinal direction.\n'
             'say: make your character say whatever succeeds this command.\n'
             'seek: move to an entity.\n'
             'exit: stop this game.\n')

    pad.dynamic('\nEnter "help" for a list of commands. '
                'Enter "help {command}" for additional information about {command}.\n', -50, erase=False, wait=False)

    while True:
        inp()
finally:
    if 'scr' in locals():
        scr.keypad(0)
        echo()
        nocbreak()
        endwin()

    with open(f'goblin.{user.name}', 'w') as f:
        for attr, value in user.__dict__.items():
            if type(value) == str:
                value = f'"{value}"'

            f.write(f'{attr}: {value}\n')
